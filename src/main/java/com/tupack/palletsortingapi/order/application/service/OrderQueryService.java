package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.dto.PageResponse;
import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.common.exception.ClientNotFoundException;
import com.tupack.palletsortingapi.common.exception.NoRoleAssignedException;
import com.tupack.palletsortingapi.common.exception.OrderNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.DocumentDto;
import com.tupack.palletsortingapi.order.application.dto.OrderDto;
import com.tupack.palletsortingapi.order.application.dto.OrderStatusUpdateDto;
import com.tupack.palletsortingapi.order.application.dto.PalletBulkDto;
import com.tupack.palletsortingapi.order.application.mapper.BulkMapper;
import com.tupack.palletsortingapi.order.application.mapper.OrderMapper;
import com.tupack.palletsortingapi.order.application.mapper.OrderPalletMapper;
import com.tupack.palletsortingapi.order.application.mapper.OrderStatusUpdateMapper;
import com.tupack.palletsortingapi.order.application.mapper.TruckMapper;
import com.tupack.palletsortingapi.order.domain.Bulk;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.OrderPallet;
import com.tupack.palletsortingapi.order.domain.OrderStatusUpdate;
import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.BulkRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderPalletRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderSpecification;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderStatusUpdateRepository;
import com.tupack.palletsortingapi.user.application.mapper.DriverMapper;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.domain.Driver;
import com.tupack.palletsortingapi.user.domain.User;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import com.tupack.palletsortingapi.utils.PackingType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderQueryService {

  private static final LocalTime START_TIME = LocalTime.of(8, 0);
  private static final LocalTime END_TIME = LocalTime.of(18, 0);

  private final OrderRepository orderRepository;
  private final BulkRepository bulkRepository;
  private final OrderPalletRepository orderPalletRepository;
  private final OrderStatusUpdateRepository orderStatusUpdateRepository;
  private final OrderStatusUpdateMapper orderStatusUpdateMapper;
  private final OrderMapper orderMapper;
  private final BulkMapper bulkMapper;
  private final OrderPalletMapper orderPalletMapper;
  private final TruckMapper truckMapper;
  private final DriverMapper driverMapper;
  private final ClientRepository clientRepository;

  public List<String> getAvailableTimeSlots(String date) {
    List<LocalTime> allSlots = generateAllTimeSlots(START_TIME, END_TIME);
    LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    List<LocalDateTime> notAvailableSlots =
        orderRepository.findNotAvailableSlots(localDate.atStartOfDay(), localDate.atTime(23, 59,
            59));
    List<LocalTime> notAvailableTimes =
        notAvailableSlots.stream().map(LocalDateTime::toLocalTime).toList();
    if (notAvailableSlots.isEmpty()) {
      return allSlots.stream().map(LocalTime::toString).toList();
    } else {
      return allSlots.stream().filter(slot -> !notAvailableTimes.contains(slot))
          .map(LocalTime::toString).toList();
    }
  }

  public GenericResponse getAllOrders(Pageable pageable, String search, List<String> statuses,
      String orderType, String pickupDateFrom, String pickupDateTo) {
    Client client = getLoggedInClient();
    User user = getLoggedUser();

    String roleName = client.getUser().getRoles().stream()
        .findFirst()
        .orElseThrow(() -> new NoRoleAssignedException(client.getUser().getId()))
        .getName();

    // Determine client/driver ID based on role
    Long clientId = null;
    Long driverId = null;

    switch (roleName) {
      case "CLIENT" -> clientId = client.getId();
      case "DRIVER" -> driverId = user.getId();
      case "ADMIN" -> {
        // ADMIN can see all orders, no additional filter needed
      }
      default -> throw new BusinessException("Invalid role: " + roleName, "INVALID_ROLE");
    }

    // Build specification with all filters
    Specification<Order> specification = OrderSpecification.buildSpecification(
        search,
        statuses,
        orderType,
        pickupDateFrom,
        pickupDateTo,
        clientId,
        driverId
    );

    // Execute query with filters
    Page<Order> orders = orderRepository.findAll(specification, pageable);

    GenericResponse response = new GenericResponse();
    response.setPageInfo(getPageInfo(orders));
    response.setData(orders.get().map(orderMapper::toDto));
    return response;
  }

  public GenericResponse getOrderById(Long orderId) {
    Order order = orderRepository.getOrderById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
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

  public GenericResponse getOrderStatus(Long orderId) {
    List<OrderStatusUpdate> orderStatusUpdate =
        orderStatusUpdateRepository.getAllByOrder_IdOrderByCreatedAtDesc(orderId);
    List<OrderStatusUpdateDto> dto =
        orderStatusUpdate.stream().map(orderStatusUpdateMapper::toDto).toList();
    return GenericResponse.success(dto);
  }

  public ResponseEntity<String> getOrderImage(Long orderId) {
    Order order = orderRepository.getOrderById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    String imageUrl = order.getSolutionImageUrl();
    if (!imageUrl.endsWith(".png")) {
      imageUrl = imageUrl + ".png";
    }
    Path imagePath = Path.of(imageUrl);
    try {
      byte[] imageBytes = Files.readAllBytes(imagePath);
      return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN)
          .body(Base64.getEncoder().encodeToString(imageBytes));
    } catch (IOException e) {
      throw new BusinessException("Order solution image not found at path: " + imageUrl,
          "IMAGE_NOT_FOUND");
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

  private User getLoggedUser() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    return (User) securityContext.getAuthentication().getPrincipal();
  }

  private Client getLoggedInClient() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    User user = (User) securityContext.getAuthentication().getPrincipal();
    return clientRepository.findClientByUserId(user.getId())
        .orElseThrow(() -> new ClientNotFoundException("userId", user.getId()));
  }

  private PageResponse<Order> getPageInfo(Page<Order> orders) {
    PageResponse<Order> pageInfo = new PageResponse<>();
    pageInfo.setCurrentPage(orders.getNumber());
    pageInfo.setPageSize(orders.getSize());
    pageInfo.setTotalElements(orders.getTotalElements());
    pageInfo.setTotalPages(orders.getTotalPages());
    return pageInfo;
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
}
