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
public class FrequentRouteDto {

  private String fromAddress;
  private String toAddress;
  private Integer frequency; // Number of times used
  private LocalDate lastUsed;
}
