package com.tupack.palletsortingapi.user.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.user.domain.User;
import com.tupack.palletsortingapi.user.application.dto.AuthResponse;
import com.tupack.palletsortingapi.user.application.dto.LoginRequest;
import com.tupack.palletsortingapi.order.application.dto.RegisterRequest;
import com.tupack.palletsortingapi.order.application.dto.TokenRefreshRequest;
import com.tupack.palletsortingapi.user.application.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
    return ResponseEntity.ok(authService.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@RequestBody @Valid TokenRefreshRequest request) {
    return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
  }

  @GetMapping("/me")
  public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
    if (user == null) {
      return ResponseEntity.status(401).build();
    }
    return ResponseEntity.ok(
            java.util.Map.of("email", user.getEmail(), "firstName", user.getFirstName(), "lastName",
                    user.getLastName(), "roles",
                    user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()));
  }
}
