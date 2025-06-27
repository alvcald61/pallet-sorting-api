package com.tupack.palletsortingapi.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolutionDto {
  private String truckId;
  private String truckDistributionImageUrl;
  private String truckDistributionUrl;
}
