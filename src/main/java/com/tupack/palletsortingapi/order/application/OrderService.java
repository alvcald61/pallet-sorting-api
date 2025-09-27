package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.order.application.dto.PalletBulkDto;
import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.application.packing.PackingStrategyExecutor;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.OrderPallet;
import com.tupack.palletsortingapi.order.domain.Pallet;
import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.domain.TruckOrder;
import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.PalletRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.TruckOrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.TruckRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.ZoneRepository;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.domain.Driver;
import com.tupack.palletsortingapi.utils.PackingType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final PackingStrategyExecutor context;
  private final ZoneRepository zoneRepository;
  private final OrderRepository orderRepository;
  private final TruckRepository truckRepository;
  private final TruckOrderRepository truckOrderRepository;
  private final PalletRepository palletRepository;

  public SolutionDto solvePacking(String packingType, SolvePackingRequest request) {
    if (packingType == null || packingType.isEmpty()) {
      throw new IllegalArgumentException("Packing type must not be null or empty");
    }
    return switch (PackingType.valueOf(packingType)) {
      case PackingType.BULK -> context.execute(PackingType.BULK.getName(), request);
      case PackingType.TWO_DIMENSIONAL ->
              context.execute(PackingType.TWO_DIMENSIONAL.getName(), request);
      case PackingType.THREE_DIMENSIONAL ->
              context.execute(PackingType.THREE_DIMENSIONAL.getName(), request);
    };
  }

  public SolutionDto scheduleOrder(String packingType, SolvePackingRequest request) {
    Order order = new Order();
    SolutionDto solution = solvePacking(packingType, request);
    order.setOrderType(PackingType.valueOf(packingType));
    List<OrderPallet> orderPallets = request.getPallets().stream()
            .map(orderPallet -> mapToOrderPallet(orderPallet, order)).toList();
    order.setOrderPallets(orderPallets);
    Client client = getLoggedInClient();
    order.setClient(client);
    order.setPickupDate(request.getDeliveryDate());
    Zone zone = zoneRepository.findById(request.getZoneId()).orElseThrow();
    order.setZone(zone);
    order.setAmount(calculateOrderAmount(solution, request, zone));
    order.setFromAddress(request.getFromAddress());
    order.setToAddress(request.getToAddress());
    order.setProjectedDeliveryDate(
            request.getDeliveryDate().plusMinutes(zone.getMaxDeliveryTime()));
    order.setTotalVolume(getTotalVolume(request));
    Truck truck = truckRepository.findById(solution.getTruckId()).orElseThrow();
    isDateAvailable(request.getDeliveryDate(), order.getProjectedDeliveryDate(),
            truck);
    orderRepository.save(order);
    TruckOrder truckOrder = new TruckOrder();
    truckOrder.setOrder(order);
    truckOrder.setTruck(truck);
    truckOrder.setDriver(getAvailableDriver(request.getDeliveryDate(), truck));
    orderRepository.save(order);
    truckOrderRepository.save(truckOrder);
    return solution;
  }

  private OrderPallet mapToOrderPallet(PalletBulkDto palletBulkDto, Order order) {
    OrderPallet orderPallet = new OrderPallet();
    Pallet pallet = palletRepository.findById(palletBulkDto.getPalletId()).orElseThrow();
    orderPallet.setPallet(pallet);
    orderPallet.setQuantity(palletBulkDto.getQuantity());
    return orderPallet;
  }

  private Driver getAvailableDriver(LocalDateTime deliveryDate, Truck truck) {
    return null;
  }

  private void isDateAvailable(LocalDateTime startDate, LocalDateTime endDate, Truck truck) {
    orderRepository.existsOrderInDateRange(startDate, endDate, truck);
  }

  private BigDecimal getTotalVolume(SolvePackingRequest request) {
    return null;
  }

  private BigDecimal calculateOrderAmount(SolutionDto solution, SolvePackingRequest request,
          Zone zone) {
    return null;
  }

  private Client getLoggedInClient() {
    return null;
  }

}
