package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DistanceCalculationRequest {

  @NotNull(message = "Origin address is required")
  @Valid
  private SimpleAddressDto origin;

  @NotNull(message = "Destination address is required")
  @Valid
  private SimpleAddressDto destination;

  private String mode; // DRIVING, WALKING, BICYCLING (default: DRIVING)
}
