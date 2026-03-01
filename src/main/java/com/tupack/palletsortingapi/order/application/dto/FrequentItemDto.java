package com.tupack.palletsortingapi.order.application.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrequentItemDto {

  private String type; // BULK, TWO_DIMENSIONAL
  private Double volume;
  private Double weight;
  private Integer quantity;
  private Integer frequency; // Number of times ordered
  private LocalDate lastUsed;
}
