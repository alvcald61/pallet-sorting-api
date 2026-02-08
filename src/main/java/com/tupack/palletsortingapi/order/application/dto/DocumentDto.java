package com.tupack.palletsortingapi.order.application.dto;

import java.io.Serializable;

/**
 * DTO for {@link com.tupack.palletsortingapi.order.domain.Document}
 */
public record DocumentDto(Long documentId, String documentName, String link, String storagePath,
    Boolean required)
    implements Serializable {

    public DocumentDto(Long documentId, String documentName, Boolean required) {
        this(documentId, documentName, null, null, required);
    }

    public DocumentDto(Long documentId, String documentName, String link, Boolean required) {
        this(documentId, documentName, link, null, required);

    }
}


