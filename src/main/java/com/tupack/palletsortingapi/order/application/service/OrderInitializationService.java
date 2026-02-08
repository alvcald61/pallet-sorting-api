package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.exception.ClientNotFoundException;
import com.tupack.palletsortingapi.common.exception.WarehouseNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.OrderDocument;
import com.tupack.palletsortingapi.order.domain.Warehouse;
import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.WarehouseRepository;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.domain.User;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import com.tupack.palletsortingapi.utils.PackingType;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for initializing orders with all required data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderInitializationService {

  private final ClientRepository clientRepository;
  private final WarehouseRepository warehouseRepository;
  private final ZoneResolverService zoneResolverService;
  private final OrderPricingService orderPricingService;
  private final OrderDocumentService orderDocumentService;

  /**
   * Initialize a new order with all required data
   *
   * @param packingType Packing type (2D, 3D, BULK)
   * @param request     Packing request data
   * @param solution    Packing solution from algorithm
   * @return Initialized order (not yet persisted)
   */
  public Order initializeOrder(String packingType, SolvePackingRequest request,
      SolutionDto solution) {

    Client client = resolveClient(request);
    Zone zone = zoneResolverService.resolveZone(request.getToAddress());
    Warehouse warehouse = resolveWarehouse(request);

    Order order = new Order();

    // Basic information
    order.setOrderType(PackingType.valueOf(packingType));
    order.setOrderStatus(OrderStatus.REVIEW);
    order.setClient(client);
    order.setZone(zone);
    order.setWarehouse(warehouse);

    // Dates
    order.setPickupDate(request.getDeliveryDate());
    order.setProjectedDeliveryDate(
        request.getDeliveryDate().plusMinutes(zone.getMaxDeliveryTime())
    );

    // Addresses
    order.setFromAddress(zoneResolverService.formatAddress(request.getFromAddress()));
    order.setToAddress(zoneResolverService.formatAddress(request.getToAddress()));
    order.setAddressLink(request.getToAddress().locationLink());

    // Cargo details
    order.setTotalVolume(BigDecimal.valueOf(request.getTotalVolume()));
    order.setTotalWeight(BigDecimal.valueOf(request.getTotalWeight()));

    // Pricing (only for trusted clients)
    if (orderPricingService.shouldCalculatePrice(client.isTrust())) {
      BigDecimal amount = orderPricingService.calculateOrderAmount(request, zone);
      order.setAmount(amount);
    }

    // Packing solution
    if (hasSolutionFiles(packingType)) {
      order.setSolution(solution.getTruckDistributionUrl());
      order.setSolutionImageUrl(solution.getTruckDistributionImageUrl());
    }

    // Documents
    List<OrderDocument> documents = orderDocumentService.createDocumentOrder(order);
    order.setDocument(documents);

    return order;
  }

  /**
   * Resolve client from request or security context
   */
  private Client resolveClient(SolvePackingRequest request) {
    if (request.getUserId() != null && !request.getUserId().isEmpty()) {
      return clientRepository.findClientByUserId(Long.valueOf(request.getUserId()))
          .orElseThrow(() -> new ClientNotFoundException("userId", Long.valueOf(request.getUserId())));
    }

    User currentUser = (User) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();

    return clientRepository.findClientByUserId(currentUser.getId())
        .orElseThrow(() -> new ClientNotFoundException("userId", currentUser.getId()));
  }

  /**
   * Resolve warehouse from request
   */
  private Warehouse resolveWarehouse(SolvePackingRequest request) {
    Long warehouseId = request.getFromAddress().warehouseId();
    return warehouseRepository.findById(warehouseId)
        .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));
  }

  /**
   * Check if packing type generates solution files
   */
  private boolean hasSolutionFiles(String packingType) {
    return packingType.equalsIgnoreCase(PackingType.TWO_DIMENSIONAL.name())
        || packingType.equalsIgnoreCase(PackingType.THREE_DIMENSIONAL.name());
  }
}
