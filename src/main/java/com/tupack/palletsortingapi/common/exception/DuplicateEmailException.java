package com.tupack.palletsortingapi.common.exception;

public class DuplicateEmailException extends BusinessException {
  public DuplicateEmailException(String email) {
    super("Email " + email + " is already registered", "DUPLICATE_EMAIL");
  }
}
