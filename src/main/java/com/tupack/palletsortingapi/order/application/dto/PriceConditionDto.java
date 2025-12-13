package com.tupack.palletsortingapi.order.application.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Value;

/**
 * DTO for {@link com.tupack.palletsortingapi.order.domain.PriceCondition}
 */
@Value
public class PriceConditionDto implements Serializable {
  Long priceConditionId;
  String currency;
  Double minWeight;
  Double maxWeight;
  Double minVolume;
  Double maxVolume;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  String createdBy;
  String updatedBy;
  boolean enabled;
}