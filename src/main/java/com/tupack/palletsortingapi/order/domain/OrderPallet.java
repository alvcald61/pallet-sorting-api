package com.tupack.palletsortingapi.order.domain;

import com.tupack.palletsortingapi.order.domain.id.OrderPalletId;
import com.tupack.palletsortingapi.order.domain.id.TruckOrderId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "orderPallet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderPallet {
  @EmbeddedId
  private OrderPalletId orderPalletId;
  @OneToOne
  @MapsId("orderId")
  private Order order;
  @ManyToOne
  @MapsId("palletId")
  private Pallet pallet;
  private int quantity;

}
