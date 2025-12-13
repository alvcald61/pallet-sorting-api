package com.tupack.palletsortingapi.order.domain.emuns;

import lombok.Getter;

@Getter
public enum OrderStatus {
  REVIEW("EN REVISIÓN"),
  PRE_APPROVED("PRE-APROBADO"),
  APPROVED("APROVADO"),
  IN_PROGRESS("EN CAMINO"),
  DELIVERED("ENTREGADO");

  private final String state;

  OrderStatus(String state) {
    this.state = state;
  }

}
