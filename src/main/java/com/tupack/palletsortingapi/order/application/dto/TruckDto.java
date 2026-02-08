package com.tupack.palletsortingapi.order.application.dto;

import com.tupack.palletsortingapi.order.domain.enums.TruckStatus;
import java.io.Serializable;
import lombok.Value;
/**
 * DTO for {@link com.tupack.palletsortingapi.order.domain.Truck}
 */
@Value
public class TruckDto implements Serializable {
	String id;
	Double width;
	Double length;
	Double height;
	TruckStatus status;
	String licensePlate;
	Double volume;
	Double weight;
	Double area;
  boolean enabled;
  Double multiplayer;
  Long driverId;
  String driverName;
}
