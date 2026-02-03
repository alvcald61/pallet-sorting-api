package com.tupack.palletsortingapi.common.exception;

/**
 * Exception thrown when a Zone is not found.
 */
public class ZoneNotFoundException extends ResourceNotFoundException {

  public ZoneNotFoundException(Long zoneId) {
    super("Zone", "id", zoneId);
  }

  public ZoneNotFoundException(String message) {
    super(message);
  }
}
