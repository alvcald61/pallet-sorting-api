package com.tupack.palletsortingapi.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressValidationResponse {

  private boolean isValid;
  private NormalizedAddressDto normalized;
  private CoordinatesDto coordinates;
  private String placeId;
  private String confidence; // HIGH, MEDIUM, LOW
}
