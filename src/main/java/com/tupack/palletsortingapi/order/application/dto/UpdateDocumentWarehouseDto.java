package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateDocumentWarehouseDto(
    @NotNull(message = "documentId es requerido")
    Long documentId,
    boolean isRequired
) {
}
