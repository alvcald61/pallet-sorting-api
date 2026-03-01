package com.tupack.palletsortingapi.order.domain.id;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class OrderPalletId implements Serializable {
  @Column(name = "orderId", nullable = false)
  private String orderId;
  @Column(name = "palletId", nullable = false)
  private String palletId;
}
