package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OrderService {

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
  private final OrderPackingService packingService;
  private final OrderSchedulingService schedulingService;
  private final OrderQueryService queryService;
  private final OrderStatusService statusService;
  private final OrderDocumentService documentService;

  public SolutionDto solvePacking(String packingType, SolvePackingRequest request) {
    return packingService.solvePacking(packingType, request);
  }

  public TwoDimensionSolutionResponse scheduleOrder(String packingType, SolvePackingRequest request) {
    return schedulingService.scheduleOrder(packingType, request);
  }

  public List<String> getAvailableTimeSlots(String date) {
    return queryService.getAvailableTimeSlots(date);
  }

  public GenericResponse getAllOrders(Pageable pageable) {
    return queryService.getAllOrders(pageable);
  }

  public GenericResponse getOrderById(Long orderId) {
    return queryService.getOrderById(orderId);
  }

  public GenericResponse getOrderStatus(Long orderId) {
    return queryService.getOrderStatus(orderId);
  }

  public ResponseEntity<String> getOrderImage(Long orderId) {
    return queryService.getOrderImage(orderId);
  }

  public GenericResponse updateOrderStatus(Long orderId, String status) {
    return statusService.updateOrderStatus(orderId, status);
  }

  public GenericResponse continueOrder(Long orderId, BigDecimal amount, String gpsLink,
      boolean denied) {
    return statusService.continueOrder(orderId, amount, gpsLink, denied);
  }

  public GenericResponse uploadDocument(Long documentId, Long orderId, MultipartFile file) {
    return documentService.uploadDocument(documentId, orderId, file);
  }
}
