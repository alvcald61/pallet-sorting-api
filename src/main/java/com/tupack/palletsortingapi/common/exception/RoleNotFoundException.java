package com.tupack.palletsortingapi.common.exception;

/**
 * Exception thrown when a Role is not found.
 */
public class RoleNotFoundException extends ResourceNotFoundException {

  public RoleNotFoundException(Long roleId) {
    super("Role", "id", roleId);
  }

  public RoleNotFoundException(String message) {
    super(message);
  }
}
