package com.tupack.palletsortingapi.order.domain.enums;

import lombok.Getter;

/**
 * Transport-specific status for tracking the physical movement and handling of cargo.
 * This provides granular tracking of the transport operation lifecycle.
 */
@Getter
public enum TransportStatus {
  PENDING("Pendiente", "Transport not yet started"),
  TRUCK_ASSIGNED("Camión Asignado", "Truck has been assigned to the order"),
  EN_ROUTE_TO_WAREHOUSE("Camino al Almacén", "Truck is traveling to pickup warehouse"),
  ARRIVED_AT_WAREHOUSE("Llegó al Almacén", "Truck arrived at warehouse"),
  LOADING("Cargando", "Cargo is being loaded onto the truck"),
  LOADING_COMPLETED("Carga Completada", "Loading operation completed"),
  EN_ROUTE_TO_DESTINATION("Camino al Destino", "Truck is traveling to delivery destination"),
  ARRIVED_AT_DESTINATION("Llegó al Destino", "Truck arrived at destination"),
  UNLOADING("Descargando", "Cargo is being unloaded from the truck"),
  UNLOADING_COMPLETED("Descarga Completada", "Unloading operation completed"),
  DELIVERED("Entregado", "Cargo successfully delivered");

  private final String displayName;
  private final String description;

  TransportStatus(String displayName, String description) {
    this.displayName = displayName;
    this.description = description;
  }

    /**
   * Check if this status represents a terminal state (no more transitions possible)
   */
  public boolean isTerminal() {
    return this == DELIVERED;
  }

  /**
   * Check if this status indicates the truck is in motion
   */
  public boolean isInTransit() {
    return this == EN_ROUTE_TO_WAREHOUSE || this == EN_ROUTE_TO_DESTINATION;
  }

  /**
   * Check if this status indicates an active operation (loading/unloading)
   */
  public boolean isActiveOperation() {
    return this == LOADING || this == UNLOADING;
  }

  /**
   * Get the next valid status transitions from the current status
   */
  public TransportStatus[] getValidNextStates() {
    return switch (this) {
      case PENDING -> new TransportStatus[]{TRUCK_ASSIGNED};
      case TRUCK_ASSIGNED -> new TransportStatus[]{EN_ROUTE_TO_WAREHOUSE};
      case EN_ROUTE_TO_WAREHOUSE -> new TransportStatus[]{ARRIVED_AT_WAREHOUSE};
      case ARRIVED_AT_WAREHOUSE -> new TransportStatus[]{LOADING};
      case LOADING -> new TransportStatus[]{LOADING_COMPLETED};
      case LOADING_COMPLETED -> new TransportStatus[]{EN_ROUTE_TO_DESTINATION};
      case EN_ROUTE_TO_DESTINATION -> new TransportStatus[]{ARRIVED_AT_DESTINATION};
      case ARRIVED_AT_DESTINATION -> new TransportStatus[]{UNLOADING};
      case UNLOADING -> new TransportStatus[]{UNLOADING_COMPLETED};
      case UNLOADING_COMPLETED -> new TransportStatus[]{DELIVERED};
      case DELIVERED -> new TransportStatus[]{};
    };
  }

  /**
   * Check if a transition from this status to the target status is valid
   */
  public boolean canTransitionTo(TransportStatus target) {
    TransportStatus[] validStates = getValidNextStates();
    for (TransportStatus validState : validStates) {
      if (validState == target) {
        return true;
      }
    }
    return false;
  }
}
