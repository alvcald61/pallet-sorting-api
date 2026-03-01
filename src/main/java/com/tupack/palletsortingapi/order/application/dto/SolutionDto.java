package com.tupack.palletsortingapi.order.application.dto;

import com.tupack.palletsortingapi.order.domain.Truck;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolutionDto {
  private Long truckId;
	private Truck truck;
  private String truckDistributionImageUrl;
  private String truckDistributionUrl;
}
