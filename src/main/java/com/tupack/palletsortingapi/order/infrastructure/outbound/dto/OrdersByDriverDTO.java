package com.tupack.palletsortingapi.order.infrastructure.outbound.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersByDriverDTO {
    private String id;
    private String driverName;
    private String name;
    private Long count;
}

