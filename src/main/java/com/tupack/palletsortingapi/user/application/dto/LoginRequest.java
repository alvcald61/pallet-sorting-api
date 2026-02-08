package com.tupack.palletsortingapi.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "El email es requerido")
    @Email(message = "Email inválido")
    @Size(max = 190, message = "El email no puede exceder 190 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String password;
}
