package com.tupack.palletsortingapi.order.application.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Value;

/**
 * DTO for {@link com.tupack.palletsortingapi.order.domain.Pallet}
 */
@Value
public class PalletDto implements Serializable {
  String id;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  String createdBy;
  String updatedBy;
  boolean enabled;
  String type;
  Double width;
  Double height;
  Double length;
  Double weight;
  boolean status;
  Integer amount;
}