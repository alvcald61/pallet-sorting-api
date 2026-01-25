package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.order.application.dto.dashboard.DashboardStatsDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByClientDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByDriverDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByStatusDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByTruckDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.PendingOrderDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.PerformanceMetricsDTO;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.emuns.OrderStatus;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.TruckRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.DriverRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final OrderRepository orderRepository;
  private final ClientRepository clientRepository;
  private final DriverRepository driverRepository;
  private final TruckRepository truckRepository;

  /**
   * Get general dashboard statistics
   */
  public DashboardStatsDTO getStats() {
    List<Order> allOrders = orderRepository.findAll();

    long totalOrders = allOrders.size();
    long pendingOrders = allOrders.stream().filter(
        order -> order.getOrderStatus() == OrderStatus.APPROVED
            || order.getOrderStatus() == OrderStatus.IN_PROGRESS).count();
    long deliveredOrders =
        allOrders.stream().filter(order -> order.getOrderStatus() == OrderStatus.DELIVERED).count();

    Double totalRevenue = allOrders.stream()
        .mapToDouble(order -> order.getAmount() != null ? order.getAmount().doubleValue() : 0.0)
        .sum();

    return DashboardStatsDTO.builder().totalOrders(totalOrders).pendingOrders(pendingOrders)
        .deliveredOrders(deliveredOrders).totalRevenue(totalRevenue).build();
  }

  /**
   * Get pending orders with optional limit
   */
  public List<PendingOrderDTO> getPendingOrders(Integer limit) {
    int actualLimit = limit != null && limit > 0 ? limit : 10;

    return orderRepository.findAll().stream().filter(
        order -> order.getOrderStatus() == OrderStatus.APPROVED
            || order.getOrderStatus() == OrderStatus.IN_PROGRESS).limit(actualLimit).map(order -> {
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
    List<Order> allOrders = orderRepository.findAll();

    return allOrders.stream().filter(order -> order.getClient() != null)
        .collect(Collectors.groupingBy(Order::getClient, Collectors.counting())).entrySet().stream()
        .map(entry -> {
          String clientName = "";
          if (entry.getKey().getUser() != null) {
            clientName = entry.getKey().getUser().getFirstName() + " " + entry.getKey().getUser()
                .getLastName();
          }
          return OrdersByClientDTO.builder().id(entry.getKey().getId().toString())
              .clientName(clientName).businessName(entry.getKey().getBusinessName())
              .count(entry.getValue()).build();
        }).collect(Collectors.toList());
  }

  /**
   * Get order count grouped by driver
   */
  public List<OrdersByDriverDTO> getOrdersByDriver() {
    List<Order> allOrders = orderRepository.findAll();

    return allOrders.stream().filter(
            order -> order.getTruck() != null
                && order.getTruck().getDriver() != null).collect(
            Collectors.groupingBy(order -> order.getTruck().getDriver(),
                Collectors.counting())).entrySet().stream().map(
            entry -> OrdersByDriverDTO.builder().id(entry.getKey().getDriverId().toString()).driverName(
                entry.getKey().getUser() != null ? entry.getKey().getUser().getFirstName() + " "
                    + entry.getKey().getUser().getLastName() : "").name(
                entry.getKey().getUser() != null ? entry.getKey().getUser().getFirstName() + " "
                    + entry.getKey().getUser().getLastName() : "").count(entry.getValue()).build())
        .collect(Collectors.toList());
  }

  /**
   * Get order count grouped by truck
   */
  public List<OrdersByTruckDTO> getOrdersByTruck() {
    List<Order> allOrders = orderRepository.findAll();

    return allOrders.stream()
        .filter(order -> order != null && order.getTruck() != null)
        .collect(
            Collectors.groupingBy(Order::getTruck, Collectors.counting()))
        .entrySet().stream().map(
            entry -> OrdersByTruckDTO.builder().id(entry.getKey().getId().toString())
                .truckPlate(entry.getKey().getLicensePlate())
                .plate(entry.getKey().getLicensePlate()).count(entry.getValue()).build())
        .collect(Collectors.toList());
  }

  /**
   * Get order count grouped by status
   */
  public List<OrdersByStatusDTO> getOrdersByStatus() {
    List<Order> allOrders = orderRepository.findAll();

    return allOrders.stream()
        .collect(Collectors.groupingBy(Order::getOrderStatus, Collectors.counting())).entrySet()
        .stream().map(entry -> OrdersByStatusDTO.builder().status(entry.getKey().name())
            .orderStatus(entry.getKey().name()).count(entry.getValue()).total(entry.getValue())
            .build()).collect(Collectors.toList());
  }

  /**
   * Get performance metrics
   */
  public PerformanceMetricsDTO getPerformanceMetrics() {
    List<Order> allOrders = orderRepository.findAll();

    Double totalVolume = allOrders.stream().mapToDouble(
        order -> order.getTotalVolume() != null ? order.getTotalVolume().doubleValue() : 0.0).sum();

    Double totalWeight = allOrders.stream().mapToDouble(
        order -> order.getTotalWeight() != null ? order.getTotalWeight().doubleValue() : 0.0).sum();

    Double totalIncome = allOrders.stream()
        .mapToDouble(order -> order.getAmount() != null ? order.getAmount().doubleValue() : 0.0)
        .sum();

    long totalOrdersCount = allOrders.size();
    Double averageDeliveryTime = 2.5; // Placeholder, adjust based on actual logic

    return PerformanceMetricsDTO.builder().totalVolume(totalVolume).totalWeight(totalWeight)
        .averageDeliveryTime(averageDeliveryTime).totalIncome(totalIncome)
        .totalOrders(totalOrdersCount).build();
  }
}

