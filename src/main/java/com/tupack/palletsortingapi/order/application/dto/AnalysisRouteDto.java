package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnalysisRouteDto {

  @NotNull(message = "From address is required")
  @Valid
  private AddressDto fromAddress;

  @NotNull(message = "To address is required")
  @Valid
  private AddressDto toAddress;
}
