package com.tupack.palletsortingapi.common.exception;

import java.time.LocalDateTime;

/**
 * Exception thrown when no truck is available for the requested specifications.
 */
public class NoTruckAvailableException extends BusinessException {

  public NoTruckAvailableException() {
    super("No truck available for the requested specifications", "NO_TRUCK_AVAILABLE");
  }

  public NoTruckAvailableException(LocalDateTime pickupDate) {
    super(String.format("No truck available for pickup date: %s", pickupDate),
        "NO_TRUCK_AVAILABLE");
  }

  public NoTruckAvailableException(String message) {
    super(message, "NO_TRUCK_AVAILABLE");
  }
}
