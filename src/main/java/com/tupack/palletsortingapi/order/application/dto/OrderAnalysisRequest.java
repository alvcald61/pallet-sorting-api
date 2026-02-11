package com.tupack.palletsortingapi.order.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class OrderAnalysisRequest {

  @NotEmpty(message = "Items list cannot be empty")
  @Valid
  private List<EstimateItemDto> items;

  @NotNull(message = "Route is required")
  @Valid
  private AnalysisRouteDto route;

  @NotNull(message = "Pickup date is required")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate pickupDate;
}
