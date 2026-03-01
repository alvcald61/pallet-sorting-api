package com.tupack.palletsortingapi.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a Role
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest implements Serializable {
  @NotBlank(message = "El nombre del rol es requerido")
  @Size(max = 100, message = "El nombre del rol no puede exceder 100 caracteres")
  private String name;
}

