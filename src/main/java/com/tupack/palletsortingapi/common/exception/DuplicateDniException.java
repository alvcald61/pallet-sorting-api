package com.tupack.palletsortingapi.common.exception;

public class DuplicateDniException extends BusinessException {
  public DuplicateDniException(String dni) {
    super("DNI " + dni + " is already registered", "DUPLICATE_DNI");
  }
}
