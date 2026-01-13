package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.order.application.DashboardService;
import com.tupack.palletsortingapi.order.application.dto.dashboard.DashboardStatsDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByClientDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByDriverDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByStatusDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByTruckDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.PendingOrderDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.PerformanceMetricsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard/stats
     * Get general dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    /**
     * GET /api/dashboard/pending-orders
     * Get pending orders with optional limit parameter
     */
    @GetMapping("/pending-orders")
    public ResponseEntity<List<PendingOrderDTO>> getPendingOrders(
            @RequestParam(name = "limit", required = false) Integer limit) {
        return ResponseEntity.ok(dashboardService.getPendingOrders(limit));
    }

    /**
     * GET /api/dashboard/orders-by-client
     * Get order count grouped by client
     */
    @GetMapping("/orders-by-client")
    public ResponseEntity<List<OrdersByClientDTO>> getOrdersByClient() {
        return ResponseEntity.ok(dashboardService.getOrdersByClient());
    }

    /**
     * GET /api/dashboard/orders-by-driver
     * Get order count grouped by driver
     */
    @GetMapping("/orders-by-driver")
    public ResponseEntity<List<OrdersByDriverDTO>> getOrdersByDriver() {
        return ResponseEntity.ok(dashboardService.getOrdersByDriver());
    }

    /**
     * GET /api/dashboard/orders-by-truck
     * Get order count grouped by truck
     */
    @GetMapping("/orders-by-truck")
    public ResponseEntity<List<OrdersByTruckDTO>> getOrdersByTruck() {
        return ResponseEntity.ok(dashboardService.getOrdersByTruck());
    }

    /**
     * GET /api/dashboard/orders-by-status
     * Get order count grouped by status
     */
    @GetMapping("/orders-by-status")
    public ResponseEntity<List<OrdersByStatusDTO>> getOrdersByStatus() {
        return ResponseEntity.ok(dashboardService.getOrdersByStatus());
    }

    /**
     * GET /api/dashboard/performance-metrics
     * Get performance metrics
     */
    @GetMapping("/performance-metrics")
    public ResponseEntity<PerformanceMetricsDTO> getPerformanceMetrics() {
        return ResponseEntity.ok(dashboardService.getPerformanceMetrics());
    }
}
