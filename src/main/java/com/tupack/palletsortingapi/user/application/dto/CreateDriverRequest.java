package com.tupack.palletsortingapi.user.application.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating/updating a Driver
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDriverRequest implements Serializable {
  private String firstName;
  private String lastName;
  private String email;
  private String password;
  private String dni;
  private String phone;
  private List<Long> roles;
}

