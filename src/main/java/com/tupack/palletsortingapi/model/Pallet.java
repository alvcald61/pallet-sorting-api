package com.tupack.palletsortingapi.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "pallet")
public class Pallet extends BaseEntity {
  private String type;
  private Double width;
  private Double length;
  private String status;
  private Integer amount;
}
