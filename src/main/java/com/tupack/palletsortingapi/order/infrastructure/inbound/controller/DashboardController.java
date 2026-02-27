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
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/dashboard/stats?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     * Get general dashboard statistics, optionally filtered by date range
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(dashboardService.getStats(startDate, endDate));
    }

    /**
     * GET /api/dashboard/pending-orders?limit=10&startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     * Get pending orders with optional limit and date range filter
     */
    @GetMapping("/pending-orders")
    public ResponseEntity<List<PendingOrderDTO>> getPendingOrders(
            @RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(dashboardService.getPendingOrders(limit, startDate, endDate));
    }

    /**
     * GET /api/dashboard/orders-by-client?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     */
    @GetMapping("/orders-by-client")
    public ResponseEntity<List<OrdersByClientDTO>> getOrdersByClient(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(dashboardService.getOrdersByClient(startDate, endDate));
    }

    /**
     * GET /api/dashboard/orders-by-driver?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     */
    @GetMapping("/orders-by-driver")
    public ResponseEntity<List<OrdersByDriverDTO>> getOrdersByDriver(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(dashboardService.getOrdersByDriver(startDate, endDate));
    }

    /**
     * GET /api/dashboard/orders-by-truck?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     */
    @GetMapping("/orders-by-truck")
    public ResponseEntity<List<OrdersByTruckDTO>> getOrdersByTruck(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(dashboardService.getOrdersByTruck(startDate, endDate));
    }

    /**
     * GET /api/dashboard/orders-by-status?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     */
    @GetMapping("/orders-by-status")
    public ResponseEntity<List<OrdersByStatusDTO>> getOrdersByStatus(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(dashboardService.getOrdersByStatus(startDate, endDate));
    }

    /**
     * GET /api/dashboard/performance-metrics?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
     */
    @GetMapping("/performance-metrics")
    public ResponseEntity<PerformanceMetricsDTO> getPerformanceMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(dashboardService.getPerformanceMetrics(startDate, endDate));
    }
}
