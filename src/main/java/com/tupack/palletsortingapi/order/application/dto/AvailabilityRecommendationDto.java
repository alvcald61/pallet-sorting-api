package com.tupack.palletsortingapi.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityRecommendationDto {

  private String preferredTimeSlot;
  private String reason;
}
