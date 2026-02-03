package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.common.exception.ClientNotFoundException;
import com.tupack.palletsortingapi.common.exception.NoTruckAvailableException;
import com.tupack.palletsortingapi.common.exception.WarehouseNotFoundException;
import com.tupack.palletsortingapi.common.exception.ZoneNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.AddressDto;
import com.tupack.palletsortingapi.order.application.dto.PalletBulkDto;
import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.application.dto.TwoDimensionSolutionResponse;
import com.tupack.palletsortingapi.order.application.mapper.TruckMapper;
import com.tupack.palletsortingapi.order.domain.Bulk;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.OrderDocument;
import com.tupack.palletsortingapi.order.domain.OrderPallet;
import com.tupack.palletsortingapi.order.domain.Pallet;
import com.tupack.palletsortingapi.order.domain.Price;
import com.tupack.palletsortingapi.order.domain.PriceCondition;
import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.domain.Warehouse;
import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.order.domain.emuns.OrderStatus;
import com.tupack.palletsortingapi.order.domain.emuns.TruckStatus;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.BulkRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderPallerRepotisoty;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PalletRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceConditionRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.TruckRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.WarehouseRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.ZoneRepository;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.domain.User;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import com.tupack.palletsortingapi.utils.PackingType;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderSchedulingService {

  private final OrderPackingService packingService;
  private final OrderDocumentService orderDocumentService;
  private final OrderStatusService orderStatusService;
  private final ZoneRepository zoneRepository;
  private final OrderRepository orderRepository;
  private final TruckRepository truckRepository;
  private final PalletRepository palletRepository;
  private final TruckMapper truckMapper;
  private final ClientRepository clientRepository;
  private final OrderPallerRepotisoty orderPalletRepository;
  private final Map<String, List<Zone>> zoneMap;
  private final BulkRepository bulkRepository;
  private final PriceConditionRepository priceConditionRepository;
  private final PriceRepository priceRepository;
  private final WarehouseRepository warehouseRepository;

  @Transactional
  public TwoDimensionSolutionResponse scheduleOrder(String packingType,
      SolvePackingRequest request) {
    SolutionDto solution = packingService.solvePacking(packingType, request);
    Client client = getClient(request);
    Order order = initializeOrder(packingType, request, client, solution);
    Truck truck = selectTruck(solution, order);
    applySolutionToOrder(packingType, solution, order);
    validateAvailability(request, order, truck);
    attachDocuments(order);
    order.setTruck(truck);
    Order finalOrder = persistOrder(order, packingType, request);
    assignTruckToOrder(truck, finalOrder);
    return buildResponse(solution);
  }

  private Truck selectTruck(SolutionDto solution, Order order) {
    Truck truck = solution.getTruck();
    if (!isTruckAvailable(truck)) {
      truck = findSimilarDimensionsTruck(solution, order);
    }
    return truck;
  }

  private void applySolutionToOrder(String packingType, SolutionDto solution, Order order) {
    if (packingType.equalsIgnoreCase(PackingType.TWO_DIMENSIONAL.name())
        || packingType.equalsIgnoreCase(PackingType.THREE_DIMENSIONAL.name())) {
      order.setSolution(solution.getTruckDistributionUrl());
      order.setSolutionImageUrl(solution.getTruckDistributionImageUrl());
    }
  }

  private void validateAvailability(SolvePackingRequest request, Order order, Truck truck) {
    isDateAvailable(request.getDeliveryDate(), order.getProjectedDeliveryDate(), truck);
  }

  private void attachDocuments(Order order) {
    List<OrderDocument> orderDocuments = orderDocumentService.createDocumentOrder(order);
    order.setDocument(orderDocuments);
  }

  private Order persistOrder(Order order, String packingType, SolvePackingRequest request) {
    Order finalOrder = orderRepository.save(order);
    orderStatusService.recordStatus(finalOrder);
    if (!packingType.equals(PackingType.BULK.getName())) {
      savePallets(request, finalOrder);
    } else {
      saveBulks(request, finalOrder);
    }
    return finalOrder;
  }

  private void assignTruckToOrder(Truck truck, Order finalOrder) {
    truck.getOrders().add(finalOrder);
    truck.setStatus(TruckStatus.ASSIGNED);
    truckRepository.save(truck);
  }

  private TwoDimensionSolutionResponse buildResponse(SolutionDto solution) {
    TwoDimensionSolutionResponse response = new TwoDimensionSolutionResponse();
    response.setImageUrl(solution.getTruckDistributionImageUrl());
    response.setTruck(truckMapper.toDto(solution.getTruck()));
    return response;
  }

  private void saveBulks(SolvePackingRequest request, Order finalOrder) {
    List<Bulk> bulkList = request.getPallets().stream().map(
            pallet -> new Bulk(finalOrder, pallet.getQuantity(), pallet.getVolume(),
                pallet.getWeight(), pallet.getHeight()))
        .toList();
    bulkRepository.saveAll(bulkList);
    finalOrder.setBulkList(bulkList);
  }

  private void savePallets(SolvePackingRequest request, Order finalOrder) {
    List<OrderPallet> orderPallets = request.getPallets().stream()
        .map(palletBulkDto -> mapToOrderPallet(palletBulkDto, finalOrder)).toList();
    orderPallets = orderPalletRepository.saveAll(orderPallets);
    finalOrder.setOrderPallets(orderPallets);
  }

  private @NonNull Order initializeOrder(String packingType, SolvePackingRequest request,
      Client client, SolutionDto solution) {
    Order order = new Order();
    order.setOrderType(PackingType.valueOf(packingType));
    order.setClient(client);
    order.setPickupDate(request.getDeliveryDate());
    Zone zone = getZoneForRequest(request);
    if (client.isTrust()) {
      order.setAmount(calculateOrderAmount(solution, request, zone));
    }
    order.setFromAddress(getAddress(request.getFromAddress()));
    order.setToAddress(getAddress(request.getToAddress()));
    order.setAddressLink(request.getToAddress().locationLink());
    Warehouse warehouse = warehouseRepository.findById(request.getFromAddress().warehouseId())
        .orElseThrow(() -> new WarehouseNotFoundException(request.getFromAddress().warehouseId()));
    order.setWarehouse(warehouse);
    order.setProjectedDeliveryDate(
        request.getDeliveryDate().plusMinutes(zone.getMaxDeliveryTime()));
    order.setZone(zone);
    order.setOrderStatus(OrderStatus.REVIEW);
    order.setTotalVolume(BigDecimal.valueOf(request.getTotalVolume()));
    order.setTotalWeight(BigDecimal.valueOf(request.getTotalWeight()));

    return order;
  }

  private Client getClient(SolvePackingRequest request) {
    if (request.getUserId() != null && !request.getUserId().isEmpty()) {
      return getUserIdClient(request.getUserId());
    }
    return getLoggedInClient();
  }

  private Client getUserIdClient(String userId) {
    return clientRepository.findClientByUserId(Long.valueOf(userId))
        .orElseThrow(() -> new ClientNotFoundException("userId", Long.valueOf(userId)));
  }

  private String getAddress(AddressDto fromAddressDto) {
    return String.format("%s, %s, %s, %s", fromAddressDto.address(), fromAddressDto.district(),
        fromAddressDto.city(), fromAddressDto.state());
  }

  private Zone getZoneForRequest(SolvePackingRequest request) {
    AddressDto toAddress = request.getToAddress();
    List<Zone> stateZone = zoneMap.get(toAddress.state().toLowerCase());
    if (stateZone == null || stateZone.isEmpty()) {
      throw new ZoneNotFoundException("No zone found for state: " + toAddress.state());
    }
    List<Zone> cityZone =
        stateZone.stream().filter(zone -> zone.getCity().equalsIgnoreCase(toAddress.city()))
            .toList();
    return cityZone.stream().filter(zone -> hasDistrict(zone, toAddress)).findFirst().orElseGet(
        () -> cityZone.stream().filter(zone -> zone.getDistrict().equalsIgnoreCase("*")).findFirst()
            .orElseThrow(() -> new ZoneNotFoundException(
                "No zone found for district: " + toAddress.district() + " in city: " + toAddress
                    .city())));
  }

  private boolean hasDistrict(Zone zone, AddressDto toAddress) {
    String[] districts = zone.getDistrict().split(",");
    for (String district : districts) {
      if (district.trim().equalsIgnoreCase(toAddress.district().trim())) {
        return true;
      }
    }
    return false;
  }

  private Truck findSimilarDimensionsTruck(SolutionDto solution, Order order) {
    return truckRepository.findSimularDimensionsTruck(solution.getTruck().getWidth(),
            solution.getTruck().getLength())
        .orElseThrow(() -> new NoTruckAvailableException(order.getPickupDate()));
  }

  private boolean isTruckAvailable(Truck truck) {
    return truck.getStatus().equals(TruckStatus.AVAILABLE);
  }

  private OrderPallet mapToOrderPallet(PalletBulkDto palletBulkDto, Order order) {
    OrderPallet orderPallet = new OrderPallet();

    Pallet pallet = palletRepository.findByWidthAndLengthAndHeight(palletBulkDto.getWidth(),
        palletBulkDto.getLength(), palletBulkDto.getHeight()).orElseGet(() -> {
      Pallet newPallet = new Pallet();
      newPallet.setWidth(palletBulkDto.getWidth());
      newPallet.setLength(palletBulkDto.getLength());
      newPallet.setHeight(palletBulkDto.getHeight());
      return palletRepository.save(newPallet);
    });
    orderPallet.setPallet(pallet);
    orderPallet.setOrder(order);
    orderPallet.setQuantity(palletBulkDto.getQuantity());
    orderPallet.setWeight(BigDecimal.valueOf(palletBulkDto.getWeight()));
    return orderPallet;
  }

  private void isDateAvailable(LocalDateTime startDate, LocalDateTime endDate, Truck truck) {
    orderRepository.existsOrderInDateRange(startDate, endDate, truck);
  }

  private BigDecimal calculateOrderAmount(SolutionDto solution, SolvePackingRequest request,
      Zone zone) {
    if (request.getToAddress().city().equalsIgnoreCase("lima")) {
      Zone requestZone =
          zoneRepository.findZoneByDistrictContainingIgnoreCase(request.getToAddress().district())
              .orElseThrow(() -> new ZoneNotFoundException(
                  "No zone found for district: " + request.getToAddress().district()));
      PriceCondition matchCondition =
          priceConditionRepository.findByVolumeAndWeight(request.getTotalVolume(),
                  request.getTotalWeight())
              .orElseThrow(() -> new BusinessException(
                  String.format("No price condition found for volume: %.2f and weight: %.2f",
                      request.getTotalVolume(), request.getTotalWeight()),
                  "PRICE_CONDITION_NOT_FOUND"));
      Price price = priceRepository.findByZoneAndPriceCondition(requestZone, matchCondition);
      return price.getPrice();
    }
    return null;
  }

  private Client getLoggedInClient() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    User user = (User) securityContext.getAuthentication().getPrincipal();
    return clientRepository.findClientByUserId(user.getId())
        .orElseThrow(() -> new ClientNotFoundException("userId", user.getId()));
  }
}
