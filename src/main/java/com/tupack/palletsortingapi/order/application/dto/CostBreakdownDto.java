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
public class CostBreakdownDto {

  private BigDecimal baseCost;
  private BigDecimal volumeCost;
  private BigDecimal weightCost;
  private BigDecimal distanceCost;
  private BigDecimal urgencyFee;
}
