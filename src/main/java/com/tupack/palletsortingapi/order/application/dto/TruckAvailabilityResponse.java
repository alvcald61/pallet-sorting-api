package com.tupack.palletsortingapi.order.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckAvailabilityResponse {

  private boolean available;
  private List<AvailableTruckDto> trucks;
  private AvailabilityRecommendationDto recommendations;
}
