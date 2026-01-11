package com.tupack.palletsortingapi.user.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.user.domain.User;
import com.tupack.palletsortingapi.user.application.dto.AuthResponse;
import com.tupack.palletsortingapi.user.application.dto.LoginRequest;
import com.tupack.palletsortingapi.order.application.dto.RegisterRequest;
import com.tupack.palletsortingapi.order.application.dto.TokenRefreshRequest;
import com.tupack.palletsortingapi.user.application.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
                    user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(), "id", user.getId().toString()));
  }

  @PostMapping("/validate")
  public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
    try {
      // Remove "Bearer " prefix if present
      String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;

      // Validate token using your JWT utility/service
      boolean isValid = authService.validateToken(jwtToken);

      if (isValid) {
        String username = authService.extractUsername(jwtToken);
        return ResponseEntity.ok(Map.of(
            "valid", true,
            "username", username
        ));
      } else {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("valid", false, "message", "Invalid token"));
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("valid", false, "message", e.getMessage()));
    }
  }

}
