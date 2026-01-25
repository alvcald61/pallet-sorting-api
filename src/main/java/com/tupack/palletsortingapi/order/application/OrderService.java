package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.order.application.dto.AddressDto;
import com.tupack.palletsortingapi.order.application.dto.DocumentDto;
import com.tupack.palletsortingapi.order.application.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.OrderDto;
import com.tupack.palletsortingapi.order.application.dto.OrderStatusUpdateDto;
import com.tupack.palletsortingapi.order.application.dto.PageResponse;
import com.tupack.palletsortingapi.order.application.dto.PalletBulkDto;
import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.application.dto.TwoDimensionSolutionResponse;
import com.tupack.palletsortingapi.order.application.mapper.BulkMapper;
import com.tupack.palletsortingapi.order.application.mapper.OrderMapper;
import com.tupack.palletsortingapi.order.application.mapper.OrderPalletMapper;
import com.tupack.palletsortingapi.order.application.mapper.OrderStatusUpdateMapper;
import com.tupack.palletsortingapi.order.application.mapper.TruckMapper;
import com.tupack.palletsortingapi.order.application.packing.PackingStrategyExecutor;
import com.tupack.palletsortingapi.order.domain.Bulk;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.OrderDocument;
import com.tupack.palletsortingapi.order.domain.OrderPallet;
import com.tupack.palletsortingapi.order.domain.Price;
import com.tupack.palletsortingapi.order.domain.PriceCondition;
import com.tupack.palletsortingapi.order.domain.Warehouse;
import com.tupack.palletsortingapi.order.domain.emuns.OrderStatus;
import com.tupack.palletsortingapi.order.domain.OrderStatusUpdate;
import com.tupack.palletsortingapi.order.domain.Pallet;
import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.domain.emuns.TruckStatus;
import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.order.domain.id.OrderDocumentId;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.BulkRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderDocumentRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderPallerRepotisoty;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderStatusUpdateRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PalletRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.TruckRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.WarehouseRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.ZoneRepository;
import com.tupack.palletsortingapi.user.application.mapper.DriverMapper;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.domain.Driver;
import com.tupack.palletsortingapi.user.domain.User;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceConditionRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.UserRepository;
import com.tupack.palletsortingapi.utils.PackingType;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final PackingStrategyExecutor context;
  private final ZoneRepository zoneRepository;
  private final OrderRepository orderRepository;
  private final TruckRepository truckRepository;
  private final PalletRepository palletRepository;
  private final TruckMapper truckMapper;
  private final UserRepository userRepository;
  private final ClientRepository clientRepository;
  private final OrderPallerRepotisoty orderPalletRepository;
  private final Map<String, List<Zone>> zoneMap;
  private final LocalTime START_TIME = LocalTime.of(8, 0);
  private final LocalTime END_TIME = LocalTime.of(18, 0);
  private final OrderMapper orderMapper;
  private final OrderStatusUpdateRepository orderStatusUpdateRepository;
  private final OrderStatusUpdateMapper orderStatusUpdateMapper;
  private final BulkRepository bulkRepository;
  private final BulkMapper bulkMapper;
  private final OrderPalletMapper orderPalletMapper;
  private final PriceConditionRepository priceConditionRepository;
  private final PriceRepository priceRepository;
  private final DriverMapper driverMapper;
  private final OrderDocumentRepository orderDocumentRepository;
  private final WarehouseRepository warehouseRepository;
  private final LocalFileUploader localFileUploader;

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

  @Transactional
  public TwoDimensionSolutionResponse scheduleOrder(String packingType,
    SolvePackingRequest request) {
    SolutionDto solution = solvePacking(packingType, request);
    Truck truck = solution.getTruck();
    Client client = getClient(request);
    Order order = initializeOrder(packingType, request, client, solution);

    if (!isTruckAvailable(truck)) {
      truck = findSimilarDimensionsTruck(solution, order);
    }

    if (packingType.equalsIgnoreCase(PackingType.TWO_DIMENSIONAL.name())
      || packingType.equalsIgnoreCase(PackingType.THREE_DIMENSIONAL.name())) {
      order.setSolution(solution.getTruckDistributionUrl());
      order.setSolutionImageUrl(solution.getTruckDistributionImageUrl());
    }

    isDateAvailable(request.getDeliveryDate(), order.getProjectedDeliveryDate(), truck);
    order.setDocument(createDocumentOrder(order));
    order.setTruck(truck);
    Order finalOrder = orderRepository.save(order);
    saveOrderStatusUpdate(finalOrder);

    if (!packingType.equals(PackingType.BULK.getName())) {
      savePallets(request, finalOrder);
    } else {
      saveBulks(request, finalOrder);
    }
    truck.getOrders().add(finalOrder);
    truck.setStatus(TruckStatus.ASSIGNED);
    truckRepository.save(truck);
    TwoDimensionSolutionResponse response = new TwoDimensionSolutionResponse();
    response.setImageUrl(solution.getTruckDistributionImageUrl());
    response.setTruck(truckMapper.toDto(solution.getTruck()));
    return response;
  }

  private List<OrderDocument> createDocumentOrder(Order finalOrder) {
    return finalOrder.getWarehouse().getDocuments().stream().map(warehouseDocument -> {
      //crear documento por cada documento del almacen
      return new OrderDocument(
        new OrderDocumentId(finalOrder.getId(), warehouseDocument.getDocumentId()),
        warehouseDocument, finalOrder, null);
    }).toList();
  }

  private void saveBulks(SolvePackingRequest request, Order finalOrder) {
    List<Bulk> bulkList = request.getPallets().stream().map(
        pallet -> new Bulk(finalOrder, pallet.getQuantity(), pallet.getVolume(), pallet.getWeight(), pallet.getHeight()))
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
    Warehouse warehouse =
      warehouseRepository.findById(request.getFromAddress().warehouseId()).orElseThrow();
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
    Client client;
    if (request.getUserId() != null && !request.getUserId().isEmpty()) {
      client = getUserIdClient(request.getUserId());
    } else {
      client = getLoggedInClient();
    }
    return client;
  }

  private Client getUserIdClient(String userId) {
    return clientRepository.findClientByUserId(Long.valueOf(userId)).orElseThrow();
  }

  private String getAddress(AddressDto fromAddressDto) {
    return String.format("%s, %s, %s, %s", fromAddressDto.address(), fromAddressDto.district(),
      fromAddressDto.city(), fromAddressDto.state());
  }

  private Zone getZoneForRequest(SolvePackingRequest request) {
    AddressDto toAddress = request.getToAddress();
    List<Zone> stateZone = zoneMap.get(toAddress.state().toLowerCase());
    if (stateZone == null || stateZone.isEmpty()) {
      throw new IllegalArgumentException("No zone found for the given state: " + toAddress.state());
    }
    List<Zone> cityZone =
      stateZone.stream().filter(zone -> zone.getCity().equalsIgnoreCase(toAddress.city())).toList();
    return cityZone.stream().filter(zone -> hasDistrict(zone, toAddress)).findFirst().orElseGet(
      () -> cityZone.stream().filter(zone -> zone.getDistrict().equalsIgnoreCase("*")).findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
          "No zone found for the given district: " + toAddress.district())));
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
      .orElseThrow(() -> new IllegalArgumentException("No truck available"));
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

  private Driver getAvailableDriver(LocalDateTime deliveryDate, Truck truck) {
    return null;
  }

  private void isDateAvailable(LocalDateTime startDate, LocalDateTime endDate, Truck truck) {
    orderRepository.existsOrderInDateRange(startDate, endDate, truck);
  }

  private BigDecimal calculateOrderAmount(SolutionDto solution, SolvePackingRequest request,
    Zone zone) {
    //    return zone.getFee().multiply(BigDecimal.valueOf(solution.getTruck().getMultiplayer()));
    if (request.getToAddress().city().equalsIgnoreCase("lima")) {
      Zone requestZone =
        zoneRepository.findZoneByDistrictContainingIgnoreCase(request.getToAddress().district())
          .orElseThrow();
      PriceCondition matchCondition =
        priceConditionRepository.findByVolumeAndWeight(request.getTotalVolume(),
          request.getTotalWeight()).orElseThrow();
      Price price = priceRepository.findByZoneAndPriceCondition(requestZone, matchCondition);
      return price.getPrice();
    }
    return null;
  }

  private Client getLoggedInClient() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    User user = (User) securityContext.getAuthentication().getPrincipal();
    return clientRepository.findClientByUserId(user.getId()).orElseThrow();
  }

  public List<String> getAvailableTimeSlots(String date) {
    List<LocalTime> allSlots = generateAllTimeSlots(START_TIME, END_TIME);
    LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    List<LocalDateTime> notAvailableSlots =
      orderRepository.findNotAvailableSlots(localDate.atStartOfDay(), localDate.atTime(23, 59, 59));
    List<LocalTime> notAvailableTimes =
      notAvailableSlots.stream().map(LocalDateTime::toLocalTime).toList();
    if (notAvailableSlots.isEmpty()) {
      return allSlots.stream().map(LocalTime::toString).toList();
    } else {
      return allSlots.stream().filter(slot -> !notAvailableTimes.contains(slot))
        .map(LocalTime::toString).toList();
    }
  }

  private List<LocalTime> generateAllTimeSlots(LocalTime startTime, LocalTime endTime) {
    List<LocalTime> slots = new ArrayList<>();
    LocalTime currentTime = startTime;
    while (!currentTime.isAfter(endTime)) {
      slots.add(currentTime);
      currentTime = currentTime.plusHours(1);
    }
    return slots;
  }

  public GenericResponse getAllOrders(Pageable pageable) {
    Client client = getLoggedInClient();
    User user = getLoggedUser();
    Page<Order> orders;

    orders = switch (client.getUser().getRoles().stream().findFirst().orElseThrow().getName()) {
      case "ADMIN" -> orderRepository.findAll(pageable);
      case "CLIENT" -> orderRepository.getAllByClientId(client.getId(), pageable);
      case "DRIVER" -> orderRepository.getAllByDriverId(user.getId(), pageable);
      default -> throw new IllegalArgumentException("Invalid role");
    };
    GenericResponse response = new GenericResponse();
    response.setPageInfo(getPageInfo(orders));
    response.setData(orders.get().map(orderMapper::toDto));
    return response;
  }

  private User getLoggedUser() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    User user = (User) securityContext.getAuthentication().getPrincipal();
    return user;
  }

  private PageResponse getPageInfo(Page<Order> orders) {
    PageResponse pageInfo = new PageResponse();
    pageInfo.setPageNumber(orders.getNumber());
    pageInfo.setPageSize(orders.getSize());
    pageInfo.setTotalElements(orders.getTotalElements());
    pageInfo.setTotalPages(orders.getTotalPages());
    return pageInfo;
  }

  public GenericResponse getOrderById(Long orderId) {
    Order order = orderRepository.getOrderById(orderId).orElseThrow();
    List<PalletBulkDto> palletBulkDtoList;
    if (order.getOrderType().equals(PackingType.BULK)) {
      List<Bulk> bulkList = bulkRepository.findAllByOrder_Id(orderId);
      palletBulkDtoList = bulkList.stream().map(bulkMapper::toDto).toList();
    } else {
      List<OrderPallet> pallets = orderPalletRepository.getAllByOrder_Id(orderId);
      palletBulkDtoList = pallets.stream().map(orderPalletMapper::toDto).toList();
    }
    List<DocumentDto> documentDtoList = loadDocuments(order);
    OrderDto orderDto = orderMapper.toDto(order);
    orderDto.setDocuments(documentDtoList);
    loadTruckAndDriver(orderDto, order);
    orderDto.setPackages(palletBulkDtoList);
    return GenericResponse.success(orderDto);
  }

  private List<DocumentDto> loadDocuments(Order order) {
    return order.getDocument().stream().map(od -> {
      var document = od.getDocument();
      return new DocumentDto(document.getDocumentId(), document.getDocumentName(), od.getLink(),
        document.getRequired());
    }).toList();
  }

  private void loadTruckAndDriver(OrderDto orderDto, Order order) {
    Truck truck = order.getTruck();
    if (truck != null) {
      orderDto.setTruck(truckMapper.toDto(truck));
      Driver driver = truck.getDriver();
      if (driver != null) {
        orderDto.setDriver(driverMapper.toDto(driver));
      }
    }
  }

  public GenericResponse getOrderStatus(Long orderId) {
    List<OrderStatusUpdate> orderStatusUpdate =
      orderStatusUpdateRepository.getAllByOrder_IdOrderByCreatedAtDesc(orderId);
    List<OrderStatusUpdateDto> dto =
      orderStatusUpdate.stream().map(orderStatusUpdateMapper::toDto).toList();
    return GenericResponse.success(dto);
  }

  public ResponseEntity<String> getOrderImage(Long orderId) {
    Order order = orderRepository.getOrderById(orderId).orElseThrow();
    String imageUrl = order.getSolutionImageUrl();
    if(!imageUrl.endsWith(".png")){
      imageUrl = imageUrl + ".png";
    }
    Path imagePath = Path.of(imageUrl);
    try {
      byte[] imageBytes = Files.readAllBytes(imagePath);
      return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
        .body(Base64.getEncoder().encodeToString(imageBytes));
    } catch (IOException e) {
      throw new IllegalArgumentException("Image not found");
    }
  }

  public GenericResponse updateOrderStatus(Long orderId, String status) {
    OrderStatus statusEnum = OrderStatus.valueOf(status);
    Order order = orderRepository.getOrderById(orderId).orElseThrow();
    if (order.getOrderStatus().equals(OrderStatus.DELIVERED) || order.getOrderStatus()
      .equals(OrderStatus.DENIED)) {
      throw new IllegalArgumentException("Order status cannot be updated");
    }
    order.setOrderStatus(statusEnum);
    orderRepository.save(order);
    saveOrderStatusUpdate(order);
    return GenericResponse.success("Order status updated successfully");
  }

  public GenericResponse continueOrder(Long orderId, BigDecimal amount, String gpsLink,
    boolean denied) {
    Order order = orderRepository.getOrderById(orderId).orElseThrow();
    OrderStatus previousStatus = order.getOrderStatus();
    switch (order.getOrderStatus()) {
      case REVIEW, PRE_APPROVED:
        updateInitialStatus(amount, order);
        if (order.getOrderStatus().equals(OrderStatus.APPROVED) && order.isDocumentPending()) {
          order.setOrderStatus(OrderStatus.DOCUMENT_PENDING);
        }
        break;
      case APPROVED:
        if (order.isDocumentPending()) {
          order.setOrderStatus(OrderStatus.DOCUMENT_PENDING);
        } else {
          order.setOrderStatus(OrderStatus.IN_PROGRESS);
          order.setGpsLink(gpsLink);
        }
        break;
      case DOCUMENT_PENDING:
        if (order.isDocumentPending()) {
          throw new IllegalArgumentException("Documents pending");
        }
        order.setOrderStatus(OrderStatus.IN_PROGRESS);
        order.setGpsLink(gpsLink);
        break;
      default:
        throw new IllegalArgumentException("Order cannot be continued");
    }
    if (denied) {
      order.setOrderStatus(OrderStatus.DENIED);
    }
    orderRepository.save(order);
    if (!previousStatus.equals(order.getOrderStatus())) {
      saveOrderStatusUpdate(order);
    }
    return GenericResponse.success("Order status updated successfully");
  }

  private static void updateInitialStatus(BigDecimal amount, Order order) {
    if (amount != null) {
      order.setAmount(amount);
      order.setOrderStatus(OrderStatus.PRE_APPROVED);
      return;
    }
    order.setOrderStatus(OrderStatus.APPROVED);
  }

  public GenericResponse uploadDocument(Long documentId, Long orderId, MultipartFile file) {
    OrderDocument orderDocument =
      orderDocumentRepository.getByOrderIdAndDocumentId(orderId, documentId).orElseThrow();
    String fileName = orderId + "-" + documentId + "-" + file.getOriginalFilename();
    try {
      String link = localFileUploader.upload(fileName, file.getBytes());
      orderDocument.setLink(link);
      orderDocumentRepository.save(orderDocument);
      Order order = orderDocument.getOrder();
      if (order.getDocument().stream().filter(od -> od.getDocument().getRequired())
        .allMatch(doc -> doc.getLink() != null)) {
        order.setDocumentPending(false);
        orderRepository.save(order);
      }
      return GenericResponse.success(link);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void saveOrderStatusUpdate(Order order) {
    orderStatusUpdateRepository.save(new OrderStatusUpdate(order, order.getOrderStatus()));
  }
}
