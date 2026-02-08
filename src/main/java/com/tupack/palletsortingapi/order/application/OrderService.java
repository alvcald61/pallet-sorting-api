package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.application.dto.TwoDimensionSolutionResponse;
import com.tupack.palletsortingapi.order.application.service.OrderDocumentService;
import com.tupack.palletsortingapi.order.application.service.OrderPackingService;
import com.tupack.palletsortingapi.order.application.service.OrderQueryService;
import com.tupack.palletsortingapi.order.application.service.OrderSchedulingService;
import com.tupack.palletsortingapi.order.application.service.OrderStatusService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

  private final OrderPackingService packingService;
  private final OrderSchedulingService schedulingService;
  private final OrderQueryService queryService;
  private final OrderStatusService statusService;
  private final OrderDocumentService documentService;

  public TwoDimensionSolutionResponse scheduleOrder(String packingType, SolvePackingRequest request) {
    log.info("Scheduling order with packing type: {}", packingType);
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
    log.info("Updating order status: orderId={}, status={}", orderId, status);
    return statusService.updateOrderStatus(orderId, status);
  }

  public GenericResponse continueOrder(Long orderId, BigDecimal amount, String gpsLink,
      boolean denied) {
    return statusService.continueOrder(orderId, amount, gpsLink, denied);
  }

  public GenericResponse uploadDocument(Long documentId, Long orderId, MultipartFile file) {
    log.info("Uploading document: documentId={}, orderId={}, filename={}", documentId, orderId, file.getOriginalFilename());
    return documentService.uploadDocument(documentId, orderId, file);
  }
}
