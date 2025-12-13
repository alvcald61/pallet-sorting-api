package com.tupack.palletsortingapi.order.domain.id;

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
public class TruckOrderId {
  @Column(name = "truckId")
  private Long truckId;

  @Column(name = "orderId")
  private Long orderId;
}
