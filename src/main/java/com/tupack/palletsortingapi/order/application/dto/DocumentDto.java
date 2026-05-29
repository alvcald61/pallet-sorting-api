package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * DTO for {@link com.tupack.palletsortingapi.order.domain.Document}.
 * Used both as a request body (create/update) and a response payload.
 * Validation annotations only fire when bound from a request.
 */
public record DocumentDto(
    Long documentId,
    @NotBlank(message = "El nombre del documento es requerido")
    @Size(max = 255)
    String documentName,
    String link,
    @Size(max = 512)
    String storagePath,
    @NotNull(message = "El indicador 'requerido' no puede ser nulo")
    Boolean required
) implements Serializable {

    public DocumentDto(Long documentId, String documentName, Boolean required) {
        this(documentId, documentName, null, null, required);
    }

    public DocumentDto(Long documentId, String documentName, String link, Boolean required) {
        this(documentId, documentName, link, null, required);
    }
}
