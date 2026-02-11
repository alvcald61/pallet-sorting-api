package com.tupack.palletsortingapi.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanceDetailDto {

  private Integer value; // Distance in meters
  private String text;   // Human-readable distance (e.g., "45.2 km")
}
