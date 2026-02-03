package com.tupack.palletsortingapi.common.exception;

/**
 * Exception thrown when a Warehouse is not found.
 */
public class WarehouseNotFoundException extends ResourceNotFoundException {

  public WarehouseNotFoundException(Long warehouseId) {
    super("Warehouse", "id", warehouseId);
  }

  public WarehouseNotFoundException(String message) {
    super(message);
  }
}
