package com.tupack.palletsortingapi.order.application.dto;

import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Value;

/**
 * DTO for {@link com.tupack.palletsortingapi.order.domain.OrderStatusUpdate}
 */
@Value
public class OrderStatusUpdateDto implements Serializable {
  Long id;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  String createdBy;
  String updatedBy;
  boolean enabled;
  OrderStatus orderStatus;
}
