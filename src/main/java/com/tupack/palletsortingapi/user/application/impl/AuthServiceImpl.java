package com.tupack.palletsortingapi.user.application.impl;

import com.tupack.palletsortingapi.user.domain.Role;
import com.tupack.palletsortingapi.user.domain.User;
import com.tupack.palletsortingapi.user.infrastructure.outbound.dabatase.RoleRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.dabatase.UserRepository;
import com.tupack.palletsortingapi.user.application.AuthService;
import com.tupack.palletsortingapi.user.application.dto.AuthResponse;
import com.tupack.palletsortingapi.user.application.dto.LoginRequest;
import com.tupack.palletsortingapi.order.application.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder encoder;
  private final AuthenticationManager authManager;
  private final JwtService jwtService;

  @Override
  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("El email ya está registrado");
    }

    Set<Role> roles = resolveRoles(request.getRoles());
    User user = User.builder().firstName(request.getFirstName()).lastName(request.getLastName())
            .email(request.getEmail().toLowerCase()).password(encoder.encode(request.getPassword()))
            .roles(roles).enabled(true).build();
    userRepository.save(user);

    return buildTokensFor(user);
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            request.getEmail().toLowerCase(), request.getPassword());
    authManager.authenticate(authToken);

    User user = userRepository.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
    return buildTokensFor(user);
  }

  @Override
  public AuthResponse refresh(String refreshToken) {
    String email = jwtService.extractSubject(refreshToken);
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Token inválido"));
    if (jwtService.isExpired(refreshToken)) {
      throw new IllegalArgumentException("Refresh token expirado");
    }
    String accessToken = jwtService.generateAccessToken(user.getEmail(), defaultClaims(user));
    return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken) // reuso
            .tokenType("Bearer").email(user.getEmail()).firstName(user.getFirstName())
            .lastName(user.getLastName())
            .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())).build();
  }

  private AuthResponse buildTokensFor(User user) {
    String accessToken = jwtService.generateAccessToken(user.getEmail(), defaultClaims(user));
    String refreshToken = jwtService.generateRefreshToken(user.getEmail());
    return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
            .tokenType("Bearer").email(user.getEmail()).firstName(user.getFirstName())
            .lastName(user.getLastName())
            .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())).build();
  }

  private Set<Role> resolveRoles(Set<String> roleNames) {
    Set<String> names = (roleNames == null || roleNames.isEmpty())
            ? Set.of("ROLE_USER")
            : roleNames;
    return names.stream().map(name -> roleRepository.findByName(name)
                    .orElseGet(() -> roleRepository.save(Role.builder().name(name).build())))
            .collect(Collectors.toSet());
  }

  private java.util.Map<String, Object> defaultClaims(User user) {
    return java.util.Map.of("sub", user.getEmail(), "roles",
            user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()), "name",
            user.getFirstName() + " " + user.getLastName());
  }
}
