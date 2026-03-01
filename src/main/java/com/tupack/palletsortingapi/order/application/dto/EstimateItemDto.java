package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class EstimateItemDto {

  @NotNull(message = "Volume is required")
  @Positive(message = "Volume must be positive")
  private Double volume;

  @NotNull(message = "Weight is required")
  @Positive(message = "Weight must be positive")
  private Double weight;

  @NotNull(message = "Quantity is required")
  @Positive(message = "Quantity must be positive")
  private Integer quantity;
}
