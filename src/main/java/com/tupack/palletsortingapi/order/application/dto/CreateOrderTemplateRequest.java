package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class CreateOrderTemplateRequest {

  @NotBlank(message = "Template name is required")
  @Size(max = 100, message = "Template name cannot exceed 100 characters")
  private String name;

  @Size(max = 500, message = "Description cannot exceed 500 characters")
  private String description;

  @NotBlank(message = "Order type is required")
  private String orderType; // BULK, TWO_DIMENSIONAL, THREE_DIMENSIONAL

  @NotEmpty(message = "Items list cannot be empty")
  @Valid
  private List<EstimateItemDto> items;

  @NotNull(message = "Default route is required")
  @Valid
  private TemplateRouteDto defaultRoute;
}
