package com.tupack.palletsortingapi.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
  private String accessToken;
  private String refreshToken;
  private String tokenType; // "Bearer"
  private String email;
  private String firstName;
  private String lastName;
  private Set<String> roles;
}
