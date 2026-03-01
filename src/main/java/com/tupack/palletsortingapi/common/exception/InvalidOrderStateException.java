package com.tupack.palletsortingapi.common.exception;

import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;;

/**
 * Exception thrown when an invalid order state transition is attempted.
 */
public class InvalidOrderStateException extends BusinessException {

  public InvalidOrderStateException(OrderStatus currentStatus) {
    super(String.format("Cannot modify order in status: %s", currentStatus),
        "INVALID_ORDER_STATE");
  }

  public InvalidOrderStateException(OrderStatus currentStatus, OrderStatus requestedStatus) {
    super(String.format("Cannot transition from %s to %s", currentStatus, requestedStatus),
        "INVALID_ORDER_STATE");
  }

  public InvalidOrderStateException(String message) {
    super(message, "INVALID_ORDER_STATE");
  }
}
