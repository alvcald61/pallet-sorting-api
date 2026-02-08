package com.tupack.palletsortingapi.common.exception;

/**
 * Exception thrown when a Document is not found.
 */
public class DocumentNotFoundException extends ResourceNotFoundException {

  public DocumentNotFoundException(Long documentId) {
    super("Document", "id", documentId);
  }

  public DocumentNotFoundException(String message) {
    super(message);
  }
}
