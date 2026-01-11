package com.tupack.palletsortingapi.order.infrastructure.outbound.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PendingOrderDTO {
  private String id;
  private String orderStatus;
  private LocalDate pickupDate;
  private String toAddress;
  private String fromAddress;
  private String clientName;
}


