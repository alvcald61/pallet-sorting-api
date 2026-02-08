package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.order.application.dto.dashboard.DashboardStatsDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByClientDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByDriverDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByStatusDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByTruckDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.PendingOrderDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.PerformanceMetricsDTO;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.TruckRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.DriverRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

  private final OrderRepository orderRepository;
  private final ClientRepository clientRepository;
  private final DriverRepository driverRepository;
  private final TruckRepository truckRepository;

  /**
   * Get general dashboard statistics
   */
  public DashboardStatsDTO getStats() {
    log.debug("Calculating dashboard statistics");

    long totalOrders = orderRepository.count();
    long pendingOrders = orderRepository.countByStatusIn(
        List.of(OrderStatus.APPROVED, OrderStatus.IN_PROGRESS));
    long deliveredOrders = orderRepository.countByStatusIn(
        List.of(OrderStatus.DELIVERED));
    Double totalRevenue = orderRepository.sumAllAmounts().doubleValue();

    log.debug("Stats calculated: totalOrders={}, pendingOrders={}, deliveredOrders={}, revenue={}",
        totalOrders, pendingOrders, deliveredOrders, totalRevenue);

    return DashboardStatsDTO.builder().totalOrders(totalOrders).pendingOrders(pendingOrders)
        .deliveredOrders(deliveredOrders).totalRevenue(totalRevenue).build();
  }

  /**
   * Get pending orders with optional limit
   */
  public List<PendingOrderDTO> getPendingOrders(Integer limit) {
    int actualLimit = limit != null && limit > 0 ? limit : 10;
    log.debug("Fetching pending orders with limit: {}", actualLimit);

    Pageable pageable = Pageable.ofSize(actualLimit);
    List<Order> pendingOrders = orderRepository.findByStatusInOrderByPickupDateAsc(
        List.of(OrderStatus.APPROVED, OrderStatus.IN_PROGRESS), pageable);

    return pendingOrders.stream().map(order -> {
      String clientName = "";
      if (order.getClient() != null && order.getClient().getUser() != null) {
        clientName = order.getClient().getUser().getFirstName() + " " + order.getClient().getUser()
            .getLastName();
      }
      return PendingOrderDTO.builder().id(order.getId().toString()).clientName(clientName)
          .fromAddress(order.getFromAddress()).toAddress(order.getToAddress())
          .pickupDate(order.getPickupDate() != null ? order.getPickupDate().toLocalDate() : null)
          .orderStatus(order.getOrderStatus().name()).build();
    }).collect(Collectors.toList());
  }

  /**
   * Get order count grouped by client
   */
  public List<OrdersByClientDTO> getOrdersByClient() {
    log.debug("Fetching orders grouped by client");
    return orderRepository.countOrdersByClient();
  }

  /**
   * Get order count grouped by driver
   */
  public List<OrdersByDriverDTO> getOrdersByDriver() {
    log.debug("Fetching orders grouped by driver");
    return orderRepository.countOrdersByDriver();
  }

  /**
   * Get order count grouped by truck
   */
  public List<OrdersByTruckDTO> getOrdersByTruck() {
    log.debug("Fetching orders grouped by truck");
    return orderRepository.countOrdersByTruck();
  }

  /**
   * Get order count grouped by status
   */
  public List<OrdersByStatusDTO> getOrdersByStatus() {
    log.debug("Fetching orders grouped by status");
    return orderRepository.countOrdersByStatus();
  }

  /**
   * Get performance metrics
   */
  public PerformanceMetricsDTO getPerformanceMetrics() {
    log.debug("Calculating performance metrics");

    Double totalVolume = orderRepository.sumTotalVolume().doubleValue();
    Double totalWeight = orderRepository.sumTotalWeight().doubleValue();
    Double totalIncome = orderRepository.sumAllAmounts().doubleValue();
    long totalOrdersCount = orderRepository.count();
    Double averageDeliveryTime = 2.5; // Placeholder, adjust based on actual logic

    log.debug("Performance metrics: volume={}, weight={}, income={}, orders={}",
        totalVolume, totalWeight, totalIncome, totalOrdersCount);

    return PerformanceMetricsDTO.builder().totalVolume(totalVolume).totalWeight(totalWeight)
        .averageDeliveryTime(averageDeliveryTime).totalIncome(totalIncome)
        .totalOrders(totalOrdersCount).build();
  }
}

