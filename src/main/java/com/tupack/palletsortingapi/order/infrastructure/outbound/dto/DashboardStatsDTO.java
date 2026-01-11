package com.tupack.palletsortingapi.order.infrastructure.outbound.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private Long totalOrders;
    private Long pendingOrders;
    private Long deliveredOrders;
    private Double totalRevenue;
}

