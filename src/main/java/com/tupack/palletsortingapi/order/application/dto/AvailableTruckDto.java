package com.tupack.palletsortingapi.order.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableTruckDto {

  private String truckId;
  private String licensePlate;
  private TruckCapacityDto capacity;
  private List<String> availableSlots;
}
