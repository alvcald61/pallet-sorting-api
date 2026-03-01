package com.tupack.palletsortingapi.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceCondition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long priceConditionId;
  private String currency;
  private Double minWeight;
  private Double maxWeight;
  private Double minVolume;
  private Double maxVolume;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String updatedBy;
  private boolean enabled;

}
