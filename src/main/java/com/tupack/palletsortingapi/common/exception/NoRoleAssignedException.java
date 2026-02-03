package com.tupack.palletsortingapi.common.exception;

/**
 * Exception thrown when a user has no roles assigned.
 */
public class NoRoleAssignedException extends BusinessException {

  public NoRoleAssignedException(Long userId) {
    super(String.format("User with id %s has no roles assigned", userId), "NO_ROLE_ASSIGNED");
  }

  public NoRoleAssignedException(String message) {
    super(message, "NO_ROLE_ASSIGNED");
  }
}
