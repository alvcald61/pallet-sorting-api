package com.tupack.palletsortingapi.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {
    @NotBlank @Size(min = 1, max = 150)
    private String firstName;

    @NotBlank @Size(min = 1, max = 150)
    private String lastName;

    @NotBlank @Email @Size(max = 190)
    private String email;

    @NotBlank @Size(min = 8, max = 100)
    private String password;

    // Opcional: nombres de roles. Si se omite, se asigna ROLE_USER
    private Set<String> roles;
}
