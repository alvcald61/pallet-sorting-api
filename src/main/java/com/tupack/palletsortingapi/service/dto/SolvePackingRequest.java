package com.tupack.palletsortingapi.service.dto;

import java.util.List;
import lombok.Data;

@Data
public class SolvePackingRequest {
  private List<PalletBulkDto> pallets;
  private Double totalWeight;
  private Double totalVolume;
}
