package com.tupack.palletsortingapi.order.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SolvePackingRequest {
  private List<PalletBulkDto> pallets;
  private Double totalWeight;
  private Double totalVolume;
  private LocalDateTime deliveryDate;
  //  private String address;
  private String city;
  private String street;
  private String zoneId;
  private String fromAddress;
  private String toAddress;
}
