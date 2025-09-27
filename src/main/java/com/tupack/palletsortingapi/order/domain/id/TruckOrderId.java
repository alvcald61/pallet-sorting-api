package com.tupack.palletsortingapi.order.domain.id;

import lombok.Data;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
public class TruckOrderId {
  @Column(name = "truckId")
  private String truckId;

  @Column(name = "orderId")
  private String orderId;
}
