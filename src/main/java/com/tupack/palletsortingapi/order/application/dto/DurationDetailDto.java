package com.tupack.palletsortingapi.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DurationDetailDto {

  private Integer value; // Duration in seconds
  private String text;   // Human-readable duration (e.g., "1h 15m")
}
