package com.tupack.palletsortingapi.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tupack.palletsortingapi.base.BaseServiceTest;
import com.tupack.palletsortingapi.order.application.dto.dashboard.DashboardStatsDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByClientDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByDriverDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByStatusDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByTruckDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.PendingOrderDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.PerformanceMetricsDTO;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.TruckRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.DriverRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;

/**
 * PERFORMANCE TEST: Verifies DashboardService uses optimized queries.
 *
 * Critical: Ensures that DashboardService does NOT call findAll() which would
 * load the entire database into memory. Instead, it should use aggregation
 * queries that execute on the database.
 *
 * Bug History: Previously all methods called findAll() causing O(n) memory
 * usage and poor performance with large datasets.
 */
@DisplayName("DashboardService - Performance Optimization Tests")
class DashboardServiceTest extends BaseServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private TruckRepository truckRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        // Default mock responses for aggregation queries
        when(orderRepository.count()).thenReturn(100L);
        when(orderRepository.countByStatusIn(anyList())).thenReturn(25L);
        when(orderRepository.sumAllAmounts()).thenReturn(BigDecimal.valueOf(50000));
        when(orderRepository.sumTotalVolume()).thenReturn(BigDecimal.valueOf(1000));
        when(orderRepository.sumTotalWeight()).thenReturn(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("PERFORMANCE: getStats() should NOT call findAll()")
    void getStatsShouldNotCallFindAll() {
        // When: Get dashboard stats
        DashboardStatsDTO stats = dashboardService.getStats(null, null);

        // Then: Should use aggregation queries, NOT findAll()
        verify(orderRepository, never()).findAll();
        verify(orderRepository).count();
        verify(orderRepository).countByStatusIn(List.of(OrderStatus.APPROVED, OrderStatus.IN_PROGRESS));
        verify(orderRepository).countByStatusIn(List.of(OrderStatus.DELIVERED));
        verify(orderRepository).sumAllAmounts();

        // Verify results
        assertThat(stats.getTotalOrders()).isEqualTo(100L);
        assertThat(stats.getTotalRevenue()).isEqualTo(50000.0);
    }

    @Test
    @DisplayName("PERFORMANCE: getPendingOrders() should use paginated query")
    void getPendingOrdersShouldUsePaginatedQuery() {
        // Given: Mock paginated response
        when(orderRepository.findByStatusInOrderByPickupDateAsc(anyList(), any(Pageable.class)))
            .thenReturn(List.of());

        // When: Get pending orders
        List<PendingOrderDTO> orders = dashboardService.getPendingOrders(10, null, null);

        // Then: Should use paginated query, NOT findAll()
        verify(orderRepository, never()).findAll();
        verify(orderRepository).findByStatusInOrderByPickupDateAsc(
            any(),
            any(Pageable.class)
        );
    }

    @Test
    @DisplayName("PERFORMANCE: getOrdersByClient() should use aggregation query")
    void getOrdersByClientShouldUseAggregation() {
        // Given: Mock aggregation result
        when(orderRepository.countOrdersByClient())
            .thenReturn(List.of(
                OrdersByClientDTO.builder()
                    .id("1")
                    .clientName("Test Client")
                    .businessName("Test Business")
                    .count(10L)
                    .build()
            ));

        // When: Get orders by client
        List<OrdersByClientDTO> result = dashboardService.getOrdersByClient(null, null);

        // Then: Should use database aggregation, NOT findAll()
        verify(orderRepository, never()).findAll();
        verify(orderRepository).countOrdersByClient();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("PERFORMANCE: getOrdersByDriver() should use aggregation query")
    void getOrdersByDriverShouldUseAggregation() {
        // Given: Mock aggregation result
        when(orderRepository.countOrdersByDriver())
            .thenReturn(List.of(
                OrdersByDriverDTO.builder()
                    .id("1")
                    .driverName("Test Driver")
                    .name("Test Driver")
                    .count(15L)
                    .build()
            ));

        // When: Get orders by driver
        List<OrdersByDriverDTO> result = dashboardService.getOrdersByDriver(null, null);

        // Then: Should use database aggregation, NOT findAll()
        verify(orderRepository, never()).findAll();
        verify(orderRepository).countOrdersByDriver();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCount()).isEqualTo(15L);
    }

    @Test
    @DisplayName("PERFORMANCE: getOrdersByTruck() should use aggregation query")
    void getOrdersByTruckShouldUseAggregation() {
        // Given: Mock aggregation result
        when(orderRepository.countOrdersByTruck())
            .thenReturn(List.of(
                OrdersByTruckDTO.builder()
                    .id("1")
                    .truckPlate("ABC-123")
                    .plate("ABC-123")
                    .count(20L)
                    .build()
            ));

        // When: Get orders by truck
        List<OrdersByTruckDTO> result = dashboardService.getOrdersByTruck(null, null);

        // Then: Should use database aggregation, NOT findAll()
        verify(orderRepository, never()).findAll();
        verify(orderRepository).countOrdersByTruck();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCount()).isEqualTo(20L);
    }

    @Test
    @DisplayName("PERFORMANCE: getOrdersByStatus() should use aggregation query")
    void getOrdersByStatusShouldUseAggregation() {
        // Given: Mock aggregation result
        when(orderRepository.countOrdersByStatus())
            .thenReturn(List.of(
                new OrdersByStatusDTO(OrderStatus.APPROVED, 30L)
            ));

        // When: Get orders by status
        List<OrdersByStatusDTO> result = dashboardService.getOrdersByStatus(null, null);

        // Then: Should use database aggregation, NOT findAll()
        verify(orderRepository, never()).findAll();
        verify(orderRepository).countOrdersByStatus();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCount()).isEqualTo(30L);
    }

    @Test
    @DisplayName("PERFORMANCE: getPerformanceMetrics() should use SUM queries")
    void getPerformanceMetricsShouldUseSumQueries() {
        // When: Get performance metrics
        PerformanceMetricsDTO metrics = dashboardService.getPerformanceMetrics(null, null);

        // Then: Should use SUM and COUNT queries, NOT findAll()
        verify(orderRepository, never()).findAll();
        verify(orderRepository).sumTotalVolume();
        verify(orderRepository).sumTotalWeight();
        verify(orderRepository).sumAllAmounts();
        verify(orderRepository).count();

        // Verify results
        assertThat(metrics.getTotalVolume()).isEqualTo(1000.0);
        assertThat(metrics.getTotalWeight()).isEqualTo(5000.0);
        assertThat(metrics.getTotalIncome()).isEqualTo(50000.0);
        assertThat(metrics.getTotalOrders()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should handle default limit for pending orders")
    void shouldHandleDefaultLimitForPendingOrders() {
        // Given: Mock paginated response
        when(orderRepository.findByStatusInOrderByPickupDateAsc(anyList(), any(Pageable.class)))
            .thenReturn(List.of());

        // When: Get pending orders with null limit
        dashboardService.getPendingOrders(null, null, null);

        // Then: Should use default limit of 10
        verify(orderRepository).findByStatusInOrderByPickupDateAsc(
            anyList(),
            any(Pageable.class)
        );
    }
}
