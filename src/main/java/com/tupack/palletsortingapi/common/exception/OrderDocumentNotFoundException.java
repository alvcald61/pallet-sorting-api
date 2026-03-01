package com.tupack.palletsortingapi.common.exception;

/**
 * Exception thrown when an OrderDocument is not found.
 */
public class OrderDocumentNotFoundException extends ResourceNotFoundException {

  public OrderDocumentNotFoundException(Long orderId, Long documentId) {
    super(String.format("OrderDocument not found with orderId: '%s' and documentId: '%s'", orderId,
        documentId));
  }

  public OrderDocumentNotFoundException(String message) {
    super(message);
  }
}
