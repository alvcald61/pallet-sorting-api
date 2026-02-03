package com.tupack.palletsortingapi.common.exception;

/**
 * Exception thrown when an Order is not found.
 */
public class OrderNotFoundException extends ResourceNotFoundException {

  public OrderNotFoundException(Long orderId) {
    super("Order", "id", orderId);
  }

  public OrderNotFoundException(String message) {
    super(message);
  }
}
