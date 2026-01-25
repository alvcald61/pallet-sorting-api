package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.application.dto.TwoDimensionSolutionResponse;
import com.tupack.palletsortingapi.user.application.mapper.DriverMapper;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.domain.Driver;
import com.tupack.palletsortingapi.user.domain.User;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import com.tupack.palletsortingapi.order.application.service.OrderDocumentService;
import com.tupack.palletsortingapi.order.application.service.OrderPackingService;
import com.tupack.palletsortingapi.order.application.service.OrderQueryService;
import com.tupack.palletsortingapi.order.application.service.OrderSchedulingService;
import com.tupack.palletsortingapi.order.application.service.OrderStatusService;
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

  private final UserRepository userRepository;
  private final ClientRepository clientRepository;
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
