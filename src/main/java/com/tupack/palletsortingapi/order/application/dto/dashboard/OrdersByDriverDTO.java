package com.tupack.palletsortingapi.order.application.dto.dashboard;

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

