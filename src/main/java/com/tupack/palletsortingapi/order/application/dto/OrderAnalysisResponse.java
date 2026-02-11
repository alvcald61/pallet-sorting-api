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
public class OrderAnalysisResponse {

  private OrderAnalysisDto analysis;
  private List<AnalysisSuggestionDto> suggestions;
  private List<AnalysisWarningDto> warnings;
}
