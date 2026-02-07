package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.order.application.OrderService;
import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.application.dto.TwoDimensionSolutionResponse;
import com.tupack.palletsortingapi.order.domain.emuns.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import com.tupack.palletsortingapi.order.application.dto.OrderDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management and pallet scheduling endpoints")
public class OrderController {

  private final OrderService orderService;


  @Operation(
      summary = "Schedule a new order",
      description = "Creates and schedules a new pallet order by solving the packing problem and assigning a truck"
  )
  @PostMapping("/solve/{packingType}")
  public TwoDimensionSolutionResponse solveOrder(
      @Valid @RequestBody SolvePackingRequest request,
      @Parameter(description = "Packing type: TWO_DIMENSIONAL, THREE_DIMENSIONAL, or BULK")
      @PathVariable String packingType) {
    // Logic to solve the order goes here
    return orderService.scheduleOrder(packingType, request);
  }

  @GetMapping("/available-slots")
  public List<String> getAvailableTimeSlots(@RequestParam String date) {
    return orderService.getAvailableTimeSlots(date);
  }

  @Operation(summary = "Get all orders", description = "Retrieves a paginated list of all orders")
  @GetMapping
  public GenericResponse getAllOrders(Pageable pageable) {
    return orderService.getAllOrders(pageable);
  }

  @Operation(summary = "Get order by ID", description = "Retrieves detailed information about a specific order")
  @GetMapping("/{orderId}")
  public GenericResponse getOrderById(
      @Parameter(description = "Order ID") @PathVariable Long orderId) {
    return orderService.getOrderById(orderId);
  }

  @Operation(summary = "Get order status", description = "Retrieves the current status of an order")
  @GetMapping("/{orderId}/status")
  public GenericResponse GetOrderStatus(
      @Parameter(description = "Order ID") @PathVariable Long orderId) {
    return orderService.getOrderStatus(orderId);
  }

  @GetMapping(value = "/{orderId}/image", produces = "text/plain")
  public ResponseEntity<String> getOrderImage(@PathVariable Long orderId) {
    return orderService.getOrderImage(orderId);
  }

  @PatchMapping("/{orderId}/status/{status}")
  public ResponseEntity<GenericResponse> updateOrderStatus(@PathVariable Long orderId,
      @PathVariable String status) {
    GenericResponse updatedSolution = orderService.updateOrderStatus(orderId, status);
    return ResponseEntity.ok(updatedSolution);
  }

  @PutMapping("/{orderId}/continue")
  public ResponseEntity<GenericResponse> processOrderContinuation(@PathVariable Long orderId,
      @RequestParam(required = false) BigDecimal amount,
      @RequestParam(required = false) String gpsLink, @RequestParam(required = false) boolean denied) {
    return ResponseEntity.ok(orderService.continueOrder(orderId, amount, gpsLink, denied));
  }


  @PostMapping(value = "/{orderId}/documents/{documentId}/upload", consumes = "multipart/form-data")
  public  ResponseEntity<GenericResponse> uploadDocument(@PathVariable Long documentId,
    @PathVariable Long orderId, @RequestParam ("file") MultipartFile file) {
    return ResponseEntity.ok(orderService.uploadDocument(documentId, orderId, file));
  }

}
