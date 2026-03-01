package com.tupack.palletsortingapi.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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
  @NotBlank(message = "El nombre es requerido")
  @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
  private String firstName;

  @NotBlank(message = "El apellido es requerido")
  @Size(max = 150, message = "El apellido no puede exceder 150 caracteres")
  private String lastName;

  @NotBlank(message = "El email es requerido")
  @Email(message = "Email inválido")
  @Size(max = 190, message = "El email no puede exceder 190 caracteres")
  private String email;

  @NotBlank(message = "La contraseña es requerida")
  @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
  private String password;

  @NotBlank(message = "El DNI es requerido")
  @Size(max = 20, message = "El DNI no puede exceder 20 caracteres")
  private String dni;

  @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
  private String phone;

  @Size(max = 30, message = "La licencia de conducir no puede exceder 30 caracteres")
  private String driverLicence;

  @NotEmpty(message = "Debe asignar al menos un rol")
  private List<Long> roles;
}

