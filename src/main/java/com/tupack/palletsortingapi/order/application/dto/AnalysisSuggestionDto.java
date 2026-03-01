package com.tupack.palletsortingapi.order.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisSuggestionDto {

  private String type; // CONSOLIDATION, TIMING, ROUTE_OPTIMIZATION
  private String message;
  private BigDecimal potentialSavings; // Optional
  private LocalDate alternativeDate; // Optional, for TIMING suggestions
}
