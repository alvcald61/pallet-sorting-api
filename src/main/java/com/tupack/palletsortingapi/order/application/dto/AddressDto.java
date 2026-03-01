package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressDto(
  @NotBlank(message = "Address is required") String address,
  @NotBlank(message = "District is required") String district,
  @NotBlank(message = "City is required") String city,
  @NotBlank(message = "State is required") String state,
  Long warehouseId,
  String locationLink) {
}
