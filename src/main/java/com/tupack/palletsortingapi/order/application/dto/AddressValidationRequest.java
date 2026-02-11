package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressValidationRequest {

  @NotBlank(message = "Address is required")
  private String address;

  @NotBlank(message = "District is required")
  private String district;

  @NotBlank(message = "City is required")
  private String city;

  @NotBlank(message = "State is required")
  private String state;
}
