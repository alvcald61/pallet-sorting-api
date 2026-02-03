package com.tupack.palletsortingapi.common.exception;

/**
 * Exception thrown when a Client is not found.
 */
public class ClientNotFoundException extends ResourceNotFoundException {

  public ClientNotFoundException(Long clientId) {
    super("Client", "id", clientId);
  }

  public ClientNotFoundException(String fieldName, Object fieldValue) {
    super("Client", fieldName, fieldValue);
  }

  public ClientNotFoundException(String message) {
    super(message);
  }
}
