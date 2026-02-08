package com.tupack.palletsortingapi.common.exception;

public class InvalidPackingRequestException extends BusinessException {
  public InvalidPackingRequestException(String message) {
    super(message, "INVALID_PACKING_REQUEST");
  }
}
