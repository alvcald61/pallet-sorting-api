package com.tupack.palletsortingapi.order.domain;

import com.tupack.palletsortingapi.order.domain.emuns.OrderStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderStatusUpdate extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "orderId")
  Order order;

  @Enumerated(EnumType.STRING)
  OrderStatus orderStatus;

}
