package com.tupack.palletsortingapi.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
public class WarehouseDocument {

  @Id
  @GeneratedValue(strategy =  GenerationType.IDENTITY)
  private Long warehouseDocumentId;

  @ManyToOne
  private Document document;
  @ManyToOne
  private Warehouse warehouse;
}
