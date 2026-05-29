package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Inbound DTO for creating/updating a Zone. Audit fields and the
 * soft-delete flag are server-managed and intentionally absent.
 */
public record ZoneRequest(
    @NotBlank(message = "El nombre de la zona es requerido")
    @Size(max = 255)
    String name,

    @Positive(message = "maxDeliveryTime debe ser positivo")
    Long maxDeliveryTime,

    @Size(max = 255)
    String zoneName,

    @NotBlank(message = "El distrito es requerido")
    @Size(max = 255)
    String district,

    @NotBlank(message = "La ciudad es requerida")
    @Size(max = 255)
    String city,

    @NotBlank(message = "El estado/región es requerido")
    @Size(max = 255)
    String state
) {
}
