package com.tupack.palletsortingapi.order.application.dto;

import java.io.Serializable;

/**
 * DTO for {@link com.tupack.palletsortingapi.order.domain.Warehouse}
 */
public record WarehouseDto(Long warehouseId, String name, String address, String phone,
    String locationLink)
    implements Serializable {
}