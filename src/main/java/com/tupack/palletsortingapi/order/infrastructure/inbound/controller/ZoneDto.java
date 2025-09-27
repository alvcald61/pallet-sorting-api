package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import java.io.Serializable;
import lombok.Value;
/**
 * DTO for {@link com.tupack.palletsortingapi.order.domain.Zone}
 */
@Value
public class ZoneDto implements Serializable {
  String id;
}