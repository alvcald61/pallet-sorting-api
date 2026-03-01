package com.tupack.palletsortingapi.order.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTemplateDto {

  private String id;
  private String name;
  private String description;
  private String orderType;
  private List<EstimateItemDto> items;
  private TemplateRouteDto defaultRoute;
  private LocalDateTime createdAt;
  private LocalDateTime lastUsed;
}
