package com.tupack.palletsortingapi.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
  private String id;
  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private String email;
  private String displayName;
  private Set<String> roles;
}
