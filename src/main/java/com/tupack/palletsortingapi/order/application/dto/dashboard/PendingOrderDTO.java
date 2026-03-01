package com.tupack.palletsortingapi.order.application.dto.dashboard;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

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


