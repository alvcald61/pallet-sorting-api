package com.tupack.palletsortingapi.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisWarningDto {

  private String type; // PEAK_HOUR, OVERWEIGHT, CAPACITY_EXCEEDED
  private String message;
  private String impact; // Description of the potential impact
}
