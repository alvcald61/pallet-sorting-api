package com.tupack.palletsortingapi.order.application.dto;

public record DocumentDto(Long documentId, String documentName, String link, Boolean required) {
}
