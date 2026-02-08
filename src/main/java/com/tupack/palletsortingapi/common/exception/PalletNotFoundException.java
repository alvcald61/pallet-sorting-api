package com.tupack.palletsortingapi.common.exception;

public class PalletNotFoundException extends ResourceNotFoundException {
  public PalletNotFoundException(Long id) {
    super("Pallet", "id", id.toString());
  }

  public PalletNotFoundException(String field, String value) {
    super("Pallet", field, value);
  }
}
