package com.tupack.palletsortingapi.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenRefreshRequest {
    @NotBlank(message = "El token de refresco es requerido")
    private String refreshToken;
}
