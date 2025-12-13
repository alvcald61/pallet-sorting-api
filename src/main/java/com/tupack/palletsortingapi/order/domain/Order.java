package com.tupack.palletsortingapi.order.domain;

import com.tupack.palletsortingapi.order.domain.emuns.OrderStatus;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.utils.PackingType;
import jakarta.persistence.EnumType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "transport_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Order extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "clientId", nullable = false)
  private Client client;
  @ManyToOne
  @JoinColumn(name = "zoneId", nullable = false)
  private Zone zone;
  @OneToMany(cascade = CascadeType.PERSIST)
  private List<OrderPallet> orderPallets;
  private LocalDateTime pickupDate;
  private String fromAddress;
  private String toAddress;
  private LocalDateTime projectedDeliveryDate;
  private LocalDateTime realDeliveryDate;
  private BigDecimal totalVolume;
  private BigDecimal totalWeight;
  @Enumerated(jakarta.persistence.EnumType.STRING)
  private PackingType orderType;
  private BigDecimal amount;
  private String solutionImageUrl;
  @Column(columnDefinition = "TEXT")
  private String solution;
  @Enumerated(EnumType.STRING)
  private OrderStatus orderStatus;
  @OneToMany
  private List<Bulk> bulkList;
  // grep -rnw '/' -e 'AFT.TRANSACTION'
}
