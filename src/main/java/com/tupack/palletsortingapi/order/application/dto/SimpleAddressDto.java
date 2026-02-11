package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SimpleAddressDto {

  @NotBlank(message = "Address is required")
  private String address;
}
