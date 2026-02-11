package com.tupack.palletsortingapi.order.application.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstimateCostResponse {

  private BigDecimal estimatedCost;
  private CostBreakdownDto breakdown;
  private DistanceInfoDto distance;
  private String currency;
}
