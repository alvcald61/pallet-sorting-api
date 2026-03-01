package com.tupack.palletsortingapi.order.domain;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "zone")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Zone extends BaseEntity{
  private String name;
  private Long maxDeliveryTime;
  private String zoneName;
  private String district;
  private String city;
  private String state;

}

