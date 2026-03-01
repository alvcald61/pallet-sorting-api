package com.tupack.palletsortingapi.common.exception;

public class TruckNotAvailableException extends BusinessException {
  public TruckNotAvailableException(Long truckId) {
    super("Truck with ID " + truckId + " is not available", "TRUCK_NOT_AVAILABLE");
  }

  public TruckNotAvailableException(String message) {
    super(message, "TRUCK_NOT_AVAILABLE");
  }
}
