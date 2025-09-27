package com.tupack.palletsortingapi.order.domain.id;

import lombok.Data;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
@Data
public class OrderPalletId {
  @Column(name = "orderId", nullable = false)
  private String orderId;
  @Column(name = "palletId", nullable = false)
  private String palletId;
}
