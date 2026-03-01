package com.tupack.palletsortingapi.common.exception;

/**
 * Exception thrown when a Truck is not found.
 */
public class TruckNotFoundException extends ResourceNotFoundException {

  public TruckNotFoundException(Long truckId) {
    super("Truck", "id", truckId);
  }

  public TruckNotFoundException(String message) {
    super(message);
  }
}
