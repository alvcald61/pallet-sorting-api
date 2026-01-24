package com.tupack.palletsortingapi.order.domain.emuns;

import lombok.Getter;

@Getter
public enum OrderStatus {
  REVIEW( "EN REVISIÓN"),
  PRE_APPROVED("PRE-APROBADO"),
  APPROVED("APROBADO"),
  DOCUMENT_PENDING("DOCUMENTOS PENDIENTES"),
  IN_PROGRESS("EN CAMINO"),
  // decidir si va aqui o es un estado separado ORDER_TRUCK_STATUS
  TRUCK_TO_INIT_WAREHOUSE("CAMINO AL ALMACEN"),
  LOADING("CARGA"),
  TRUCK_TO_DESTINATION("CAMINO AL DESTINO"),
  UNLOADING("DESCARGA"),
  DELIVERED("ENTREGADO"),
  DENIED("DENEGADO");

  private final String state;

  OrderStatus(String state) {
    this.state = state;
  }

}
