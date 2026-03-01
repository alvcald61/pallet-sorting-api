package com.tupack.palletsortingapi.order.application.service;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

  private final OrderRepository orderRepository;
  private final ClientRepository clientRepository;
  private final DriverRepository driverRepository;
  private final TruckRepository truckRepository;

  /**
   * Converts optional LocalDate params to LocalDateTime range boundaries.
   * Returns null if startDate is null (meaning no filter).
   */
  private LocalDateTime toStartOfDay(LocalDate date) {
    return date != null ? date.atStartOfDay() : null;
  }

  private LocalDateTime toEndOfDay(LocalDate date) {
    return date != null ? date.atTime(23, 59, 59) : null;
  }

  /**
   * Get general dashboard statistics
   */
  public DashboardStatsDTO getStats(LocalDate startDate, LocalDate endDate) {
    log.debug("Calculating dashboard statistics, startDate={}, endDate={}", startDate, endDate);

    long totalOrders;
    long pendingOrders;
    long deliveredOrders;
    Double totalRevenue;

    if (startDate != null && endDate != null) {
      LocalDateTime start = toStartOfDay(startDate);
      LocalDateTime end = toEndOfDay(endDate);
      totalOrders = orderRepository.countByStatusInAndDateRange(List.of(OrderStatus.values()), start, end);
      pendingOrders = orderRepository.countByStatusInAndDateRange(
          List.of(OrderStatus.APPROVED, OrderStatus.IN_PROGRESS), start, end);
      deliveredOrders = orderRepository.countByStatusInAndDateRange(
          List.of(OrderStatus.DELIVERED), start, end);
      totalRevenue = orderRepository.sumAllAmountsInDateRange(start, end).doubleValue();
    } else {
      totalOrders = orderRepository.count();
      pendingOrders = orderRepository.countByStatusIn(
          List.of(OrderStatus.APPROVED, OrderStatus.IN_PROGRESS));
      deliveredOrders = orderRepository.countByStatusIn(List.of(OrderStatus.DELIVERED));
      totalRevenue = orderRepository.sumAllAmounts().doubleValue();
    }

    return DashboardStatsDTO.builder().totalOrders(totalOrders).pendingOrders(pendingOrders)
        .deliveredOrders(deliveredOrders).totalRevenue(totalRevenue).build();
  }

  /**
   * Get pending orders with optional limit and date filter
   */
  public List<PendingOrderDTO> getPendingOrders(Integer limit, LocalDate startDate, LocalDate endDate) {
    int actualLimit = limit != null && limit > 0 ? limit : 10;
    log.debug("Fetching pending orders with limit: {}", actualLimit);

    Pageable pageable = Pageable.ofSize(actualLimit);
    List<Order> pendingOrders;

    if (startDate != null && endDate != null) {
      pendingOrders = orderRepository.findByStatusInAndDateRangeOrderByPickupDateAsc(
          List.of(OrderStatus.APPROVED, OrderStatus.IN_PROGRESS),
          toStartOfDay(startDate), toEndOfDay(endDate), pageable);
    } else {
      pendingOrders = orderRepository.findByStatusInOrderByPickupDateAsc(
          List.of(OrderStatus.APPROVED, OrderStatus.IN_PROGRESS), pageable);
    }

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
  public List<OrdersByClientDTO> getOrdersByClient(LocalDate startDate, LocalDate endDate) {
    log.debug("Fetching orders grouped by client");
    if (startDate != null && endDate != null) {
      return orderRepository.countOrdersByClientInDateRange(toStartOfDay(startDate), toEndOfDay(endDate));
    }
    return orderRepository.countOrdersByClient();
  }

  /**
   * Get order count grouped by driver
   */
  public List<OrdersByDriverDTO> getOrdersByDriver(LocalDate startDate, LocalDate endDate) {
    log.debug("Fetching orders grouped by driver");
    if (startDate != null && endDate != null) {
      return orderRepository.countOrdersByDriverInDateRange(toStartOfDay(startDate), toEndOfDay(endDate));
    }
    return orderRepository.countOrdersByDriver();
  }

  /**
   * Get order count grouped by truck
   */
  public List<OrdersByTruckDTO> getOrdersByTruck(LocalDate startDate, LocalDate endDate) {
    log.debug("Fetching orders grouped by truck");
    if (startDate != null && endDate != null) {
      return orderRepository.countOrdersByTruckInDateRange(toStartOfDay(startDate), toEndOfDay(endDate));
    }
    return orderRepository.countOrdersByTruck();
  }

  /**
   * Get order count grouped by status
   */
  public List<OrdersByStatusDTO> getOrdersByStatus(LocalDate startDate, LocalDate endDate) {
    log.debug("Fetching orders grouped by status");
    if (startDate != null && endDate != null) {
      return orderRepository.countOrdersByStatusInDateRange(toStartOfDay(startDate), toEndOfDay(endDate));
    }
    return orderRepository.countOrdersByStatus();
  }

  /**
   * Get performance metrics
   */
  public PerformanceMetricsDTO getPerformanceMetrics(LocalDate startDate, LocalDate endDate) {
    log.debug("Calculating performance metrics");

    Double totalVolume;
    Double totalWeight;
    Double totalIncome;
    long totalOrdersCount;

    if (startDate != null && endDate != null) {
      LocalDateTime start = toStartOfDay(startDate);
      LocalDateTime end = toEndOfDay(endDate);
      totalVolume = orderRepository.sumTotalVolumeInDateRange(start, end).doubleValue();
      totalWeight = orderRepository.sumTotalWeightInDateRange(start, end).doubleValue();
      totalIncome = orderRepository.sumAllAmountsInDateRange(start, end).doubleValue();
      totalOrdersCount = orderRepository.countByStatusInAndDateRange(List.of(OrderStatus.values()), start, end);
    } else {
      totalVolume = orderRepository.sumTotalVolume().doubleValue();
      totalWeight = orderRepository.sumTotalWeight().doubleValue();
      totalIncome = orderRepository.sumAllAmounts().doubleValue();
      totalOrdersCount = orderRepository.count();
    }

    Double averageDeliveryTime = 2.5; // Placeholder

    return PerformanceMetricsDTO.builder().totalVolume(totalVolume).totalWeight(totalWeight)
        .averageDeliveryTime(averageDeliveryTime).totalIncome(totalIncome)
        .totalOrders(totalOrdersCount).build();
  }
}
