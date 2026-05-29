package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Inbound DTO for creating/updating a PriceCondition. Audit fields and
 * the soft-delete flag are server-managed and intentionally absent.
 */
public record PriceConditionRequest(
    @NotBlank(message = "La moneda es requerida")
    @Size(min = 3, max = 8)
    String currency,

    @PositiveOrZero(message = "minWeight debe ser >= 0")
    Double minWeight,

    @PositiveOrZero(message = "maxWeight debe ser >= 0")
    Double maxWeight,

    @PositiveOrZero(message = "minVolume debe ser >= 0")
    Double minVolume,

    @PositiveOrZero(message = "maxVolume debe ser >= 0")
    Double maxVolume
) {
}
