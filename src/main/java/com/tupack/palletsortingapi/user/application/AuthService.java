package com.tupack.palletsortingapi.user.application;

import com.tupack.palletsortingapi.user.application.dto.AuthResponse;
import com.tupack.palletsortingapi.user.application.dto.LoginRequest;
import com.tupack.palletsortingapi.order.application.dto.RegisterRequest;

public interface AuthService {
  AuthResponse register(RegisterRequest request);

  AuthResponse login(LoginRequest request);

  AuthResponse refresh(String refreshToken);

  boolean validateToken(String jwtToken);

  String extractUsername(String jwtToken);
}
