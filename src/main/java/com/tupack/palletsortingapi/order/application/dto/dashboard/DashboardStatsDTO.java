package com.tupack.palletsortingapi.order.application.dto.dashboard;

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

