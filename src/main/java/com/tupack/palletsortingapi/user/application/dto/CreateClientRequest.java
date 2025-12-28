package com.tupack.palletsortingapi.user.application.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a Client with associated User
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClientRequest implements Serializable {
  private String firstName;
  private String lastName;
  private String email;
  private String password;
  private String ruc;
  private String businessName;
  private String phone;
  private String address;
  private boolean trust;
  private List<Long> roles;
}

