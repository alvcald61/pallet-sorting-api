package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * DTO for {@link com.tupack.palletsortingapi.order.domain.Warehouse}.
 * Used both as a request body (create/update) and a response payload.
 * Validation annotations only fire when bound from a request.
 */
public record WarehouseDto(
    Long warehouseId,
    @NotBlank(message = "El nombre es requerido")
    @Size(max = 255)
    String name,
    @NotBlank(message = "La dirección es requerida")
    @Size(max = 255)
    String address,
    @NotBlank(message = "El teléfono es requerido")
    @Size(max = 32)
    String phone,
    @Size(max = 512)
    String locationLink,
    @NotBlank(message = "El distrito es requerido")
    @Size(max = 255)
    String district,
    @NotBlank(message = "La ciudad es requerida")
    @Size(max = 255)
    String city,
    @NotBlank(message = "El estado/región es requerido")
    @Size(max = 255)
    String state
) implements Serializable {
}
