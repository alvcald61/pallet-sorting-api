package com.tupack.palletsortingapi.service;

import com.tupack.palletsortingapi.service.dto.AuthResponse;
import com.tupack.palletsortingapi.service.dto.LoginRequest;
import com.tupack.palletsortingapi.service.dto.RegisterRequest;

public interface AuthService {
  AuthResponse register(RegisterRequest request);

  AuthResponse login(LoginRequest request);

  AuthResponse refresh(String refreshToken);
}
