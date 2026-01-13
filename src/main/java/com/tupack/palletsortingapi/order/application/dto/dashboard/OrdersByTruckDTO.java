package com.tupack.palletsortingapi.order.application.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersByTruckDTO {
    private String id;
    private String truckPlate;
    private String plate;
    private Long count;
}

