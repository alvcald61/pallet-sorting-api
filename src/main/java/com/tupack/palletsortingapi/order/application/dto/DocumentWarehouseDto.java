package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record DocumentWarehouseDto(
    @NotEmpty(message = "La lista de documentos no puede estar vacía")
    @Valid
    List<UpdateDocumentWarehouseDto> documents
) {
}
