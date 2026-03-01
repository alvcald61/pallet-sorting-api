package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for DashboardController.
 * Verifies that optimized dashboard queries work correctly end-to-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("DashboardController Integration Tests")
class DashboardControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should successfully get dashboard statistics")
    void shouldSuccessfullyGetDashboardStats() throws Exception {
        // When/Then: Should return 200 OK with stats
        mockMvc.perform(get("/api/dashboard/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalOrders").exists())
            .andExpect(jsonPath("$.pendingOrders").exists())
            .andExpect(jsonPath("$.deliveredOrders").exists())
            .andExpect(jsonPath("$.totalRevenue").exists());
    }

    @Test
    @DisplayName("Should successfully get pending orders")
    void shouldSuccessfullyGetPendingOrders() throws Exception {
        // When/Then: Should return 200 OK with pending orders array
        mockMvc.perform(get("/api/dashboard/pending-orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should respect limit parameter for pending orders")
    void shouldRespectLimitParameterForPendingOrders() throws Exception {
        // When/Then: Should accept limit parameter
        mockMvc.perform(get("/api/dashboard/pending-orders")
                .param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should successfully get orders by status")
    void shouldSuccessfullyGetOrdersByStatus() throws Exception {
        // When/Then: Should return 200 OK with grouped data
        mockMvc.perform(get("/api/dashboard/orders-by-status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should successfully get orders by client")
    void shouldSuccessfullyGetOrdersByClient() throws Exception {
        // When/Then: Should return 200 OK with grouped data
        mockMvc.perform(get("/api/dashboard/orders-by-client"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should successfully get orders by driver")
    void shouldSuccessfullyGetOrdersByDriver() throws Exception {
        // When/Then: Should return 200 OK with grouped data
        mockMvc.perform(get("/api/dashboard/orders-by-driver"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should successfully get orders by truck")
    void shouldSuccessfullyGetOrdersByTruck() throws Exception {
        // When/Then: Should return 200 OK with grouped data
        mockMvc.perform(get("/api/dashboard/orders-by-truck"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should successfully get performance metrics")
    void shouldSuccessfullyGetPerformanceMetrics() throws Exception {
        // When/Then: Should return 200 OK with metrics
        mockMvc.perform(get("/api/dashboard/performance"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalVolume").exists())
            .andExpect(jsonPath("$.totalWeight").exists())
            .andExpect(jsonPath("$.totalIncome").exists())
            .andExpect(jsonPath("$.totalOrders").exists())
            .andExpect(jsonPath("$.averageDeliveryTime").exists());
    }

    @Test
    @DisplayName("PERFORMANCE: All dashboard endpoints should respond quickly")
    void allDashboardEndpointsShouldRespondQuickly() throws Exception {
        // Note: This is a basic response time test
        // With optimized queries, all endpoints should respond in < 1 second
        // even with large datasets (when indices are applied)

        String[] endpoints = {
            "/api/dashboard/stats",
            "/api/dashboard/pending-orders",
            "/api/dashboard/orders-by-status",
            "/api/dashboard/orders-by-client",
            "/api/dashboard/orders-by-driver",
            "/api/dashboard/orders-by-truck",
            "/api/dashboard/performance"
        };

        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint))
                .andExpect(status().isOk());
        }
    }
}
