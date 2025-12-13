package com.tupack.palletsortingapi.order.domain;

import com.tupack.palletsortingapi.order.domain.id.OrderPalletId;
import com.tupack.palletsortingapi.order.domain.id.TruckOrderId;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
//  @EmbeddedId
//  private OrderPalletId orderPalletId;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne()
  @JoinColumn(name = "orderId")
  private Order order;

	@ManyToOne
  @JoinColumn(name = "palletId")
  private Pallet pallet;
  private int quantity;
  private BigDecimal weight;


}
