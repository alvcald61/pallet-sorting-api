package com.tupack.palletsortingapi.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 1, max = 150, message = "El nombre debe tener entre 1 y 150 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es requerido")
    @Size(min = 1, max = 150, message = "El apellido debe tener entre 1 y 150 caracteres")
    private String lastName;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email inválido")
    @Size(max = 190, message = "El email no puede exceder 190 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    private String password;

    // Opcional: nombres de roles. Si se omite, se asigna ROLE_USER
    private Set<String> roles;
}
