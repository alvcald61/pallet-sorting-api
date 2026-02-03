package com.tupack.palletsortingapi.common.exception;

/**
 * Exception thrown when a JWT token is invalid or expired.
 */
public class InvalidTokenException extends BusinessException {

  public InvalidTokenException() {
    super("Invalid or expired token", "INVALID_TOKEN");
  }

  public InvalidTokenException(String message) {
    super(message, "INVALID_TOKEN");
  }
}
