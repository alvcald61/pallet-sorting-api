package com.tupack.palletsortingapi.order.application.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Value;

/**
 * DTO for {@link com.tupack.palletsortingapi.order.domain.Zone}
 */
@Value
public class ZoneDto implements Serializable {
  Long id;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  String createdBy;
  String updatedBy;
  boolean enabled;
  String name;
  Long maxDeliveryTime;
  String zoneName;
  String district;
  String city;
  String state;
}