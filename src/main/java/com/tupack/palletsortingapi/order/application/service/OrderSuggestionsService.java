package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.exception.ClientNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.FrequentItemDto;
import com.tupack.palletsortingapi.order.application.dto.FrequentRouteDto;
import com.tupack.palletsortingapi.order.application.dto.OrderSuggestionsResponse;
import com.tupack.palletsortingapi.order.application.dto.OrderTemplateDto;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Service responsible for generating order suggestions based on client history.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSuggestionsService {

  private final OrderRepository orderRepository;
  private final ClientRepository clientRepository;

  /**
   * Get order suggestions for a user
   *
   * @param userId User ID
   * @param limit  Maximum number of suggestions per category
   * @return Order suggestions with frequent items, routes, and templates
   */
  public OrderSuggestionsResponse getSuggestions(Long userId, Integer limit) {
    log.info("Getting order suggestions for user ID: {}", userId);

    // Verify client exists
    Client client = clientRepository.findClientByUserId(userId)
        .orElseThrow(() -> new ClientNotFoundException("userId", userId));

    // Get client's order history (last 100 orders)
    Pageable pageable = PageRequest.of(0, 100);
    Page<Order> ordersPage = orderRepository.getAllByClientId(client.getId(), pageable);
    List<Order> orders = ordersPage.getContent();

    // Generate suggestions
    List<FrequentItemDto> frequentItems = analyzeFrequentItems(orders, limit);
    List<FrequentRouteDto> frequentRoutes = analyzeFrequentRoutes(orders, limit);
    List<OrderTemplateDto> templates = new ArrayList<>(); // Will be populated from actual templates

    return OrderSuggestionsResponse.builder()
        .frequentItems(frequentItems)
        .frequentRoutes(frequentRoutes)
        .templates(templates)
        .build();
  }

  /**
   * Analyze order history to find frequently ordered items
   */
  private List<FrequentItemDto> analyzeFrequentItems(List<Order> orders, Integer limit) {
    // Group orders by volume/weight combination
    Map<String, ItemFrequency> itemFrequencyMap = new HashMap<>();

    for (Order order : orders) {
      String key = generateItemKey(order);
      itemFrequencyMap.computeIfAbsent(key, k -> new ItemFrequency(order))
          .increment(order.getPickupDate());
    }

    // Convert to DTOs and sort by frequency
    return itemFrequencyMap.values().stream()
        .sorted(Comparator.comparingInt(ItemFrequency::getFrequency).reversed())
        .limit(limit != null ? limit : 5)
        .map(ItemFrequency::toDto)
        .collect(Collectors.toList());
  }

  /**
   * Analyze order history to find frequently used routes
   */
  private List<FrequentRouteDto> analyzeFrequentRoutes(List<Order> orders, Integer limit) {
    // Group orders by route (from -> to address)
    Map<String, RouteFrequency> routeFrequencyMap = new HashMap<>();

    for (Order order : orders) {
      String key = generateRouteKey(order);
      routeFrequencyMap.computeIfAbsent(key, k -> new RouteFrequency(order))
          .increment(order.getPickupDate());
    }

    // Convert to DTOs and sort by frequency
    return routeFrequencyMap.values().stream()
        .sorted(Comparator.comparingInt(RouteFrequency::getFrequency).reversed())
        .limit(limit != null ? limit : 5)
        .map(RouteFrequency::toDto)
        .collect(Collectors.toList());
  }

  /**
   * Generate unique key for item grouping
   */
  private String generateItemKey(Order order) {
    // Round volume and weight to nearest 5 for grouping similar items
    long volumeKey = Math.round(order.getTotalVolume().doubleValue() / 5) * 5;
    long weightKey = Math.round(order.getTotalWeight().doubleValue() / 5) * 5;
    return order.getOrderType() + "_" + volumeKey + "_" + weightKey;
  }

  /**
   * Generate unique key for route grouping
   */
  private String generateRouteKey(Order order) {
    return order.getFromAddress() + "||" + order.getToAddress();
  }

  /**
   * Helper class to track item frequency
   */
  private static class ItemFrequency {
    private final Order sampleOrder;
    private int frequency = 0;
    private LocalDate lastUsed;

    public ItemFrequency(Order order) {
      this.sampleOrder = order;
      this.lastUsed = order.getPickupDate().toLocalDate();
    }

    public void increment(java.time.LocalDateTime pickupDate) {
      frequency++;
      LocalDate orderDate = pickupDate.toLocalDate();
      if (lastUsed == null || orderDate.isAfter(lastUsed)) {
        lastUsed = orderDate;
      }
    }

    public int getFrequency() {
      return frequency;
    }

    public FrequentItemDto toDto() {
      return FrequentItemDto.builder()
          .type(sampleOrder.getOrderType().name())
          .volume(sampleOrder.getTotalVolume().doubleValue())
          .weight(sampleOrder.getTotalWeight().doubleValue())
          .quantity(1) // Simplified - could be calculated from cargo
          .frequency(frequency)
          .lastUsed(lastUsed)
          .build();
    }
  }

  /**
   * Helper class to track route frequency
   */
  private static class RouteFrequency {
    private final String fromAddress;
    private final String toAddress;
    private int frequency = 0;
    private LocalDate lastUsed;

    public RouteFrequency(Order order) {
      this.fromAddress = order.getFromAddress();
      this.toAddress = order.getToAddress();
      this.lastUsed = order.getPickupDate().toLocalDate();
    }

    public void increment(java.time.LocalDateTime pickupDate) {
      frequency++;
      LocalDate orderDate = pickupDate.toLocalDate();
      if (lastUsed == null || orderDate.isAfter(lastUsed)) {
        lastUsed = orderDate;
      }
    }

    public int getFrequency() {
      return frequency;
    }

    public FrequentRouteDto toDto() {
      return FrequentRouteDto.builder()
          .fromAddress(fromAddress)
          .toAddress(toAddress)
          .frequency(frequency)
          .lastUsed(lastUsed)
          .build();
    }
  }
}
