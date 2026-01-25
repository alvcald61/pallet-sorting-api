package com.tupack.palletsortingapi.order.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Value;

/**
 * DTO for {@link com.tupack.palletsortingapi.order.domain.Price}
 */
@Value
public class PriceDto implements Serializable {
  Long priceId;
  PriceConditionDto priceCondition;
  ZoneDto zone;
  BigDecimal price;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  String createdBy;
  String updatedBy;
  boolean enabled;
}
