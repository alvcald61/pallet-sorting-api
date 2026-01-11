package com.tupack.palletsortingapi.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
public class Warehouse {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long warehouseId;
  private String name;
  private String address; // address, district, city, state
  private String district;
  private String city;
  private String state;
  private String phone;
  private String locationLink;

}
