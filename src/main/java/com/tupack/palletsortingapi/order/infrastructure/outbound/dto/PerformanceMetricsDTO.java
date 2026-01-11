package com.tupack.palletsortingapi.order.infrastructure.outbound.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceMetricsDTO {
    private Double totalVolume;
    private Double totalWeight;
    private Double averageDeliveryTime;
    private Double totalIncome;
    private Long totalOrders;
}

