package com.tupack.palletsortingapi.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormalizedAddressDto {

  private String address;
  private String district;
  private String city;
  private String state;
  private String country;
}
