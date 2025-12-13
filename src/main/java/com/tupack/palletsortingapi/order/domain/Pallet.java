package com.tupack.palletsortingapi.order.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "pallet")
public class Pallet extends BaseEntity {
  private String type;
  private Double width;
  private Double length;
  private Double height;
  private Integer amount;
}
