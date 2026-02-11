package com.tupack.palletsortingapi.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAnalysisDto {

  private Double totalVolume;
  private Double totalWeight;
  private Double truckUtilization; // 0.0 to 1.0
  private Integer optimizationScore; // 0 to 100
}
