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
public class OrderSuggestionsResponse {

  private List<FrequentItemDto> frequentItems;
  private List<FrequentRouteDto> frequentRoutes;
  private List<OrderTemplateDto> templates;
}
