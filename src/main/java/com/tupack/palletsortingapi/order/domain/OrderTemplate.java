package com.tupack.palletsortingapi.order.domain;

import com.tupack.palletsortingapi.user.domain.Client;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTemplate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "client_id", nullable = false)
  private Client client;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 500)
  private String description;

  @Column(nullable = false, length = 50)
  private String orderType; // BULK, TWO_DIMENSIONAL, THREE_DIMENSIONAL

  @Column(columnDefinition = "TEXT")
  private String itemsJson; // JSON string with item details

  @Column(columnDefinition = "TEXT")
  private String routeJson; // JSON string with route details

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column
  private LocalDateTime lastUsed;
}
