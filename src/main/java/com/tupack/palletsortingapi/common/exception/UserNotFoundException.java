package com.tupack.palletsortingapi.common.exception;

/**
 * Exception thrown when a User is not found.
 */
public class UserNotFoundException extends ResourceNotFoundException {

  public UserNotFoundException(Long userId) {
    super("User", "id", userId);
  }

  public UserNotFoundException(String fieldName, Object fieldValue) {
    super("User", fieldName, fieldValue);
  }

  public UserNotFoundException(String message) {
    super(message);
  }
}
