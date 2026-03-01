package com.tupack.palletsortingapi.common.exception;

/**
 * Exception thrown when a Driver is not found.
 */
public class DriverNotFoundException extends ResourceNotFoundException {

  public DriverNotFoundException(Long driverId) {
    super("Driver", "id", driverId);
  }

  public DriverNotFoundException(String message) {
    super(message);
  }
}
