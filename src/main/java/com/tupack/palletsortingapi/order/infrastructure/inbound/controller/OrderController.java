package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.order.application.OrderService;
import com.tupack.palletsortingapi.order.application.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.application.dto.TwoDimensionSolutionResponse;
import com.tupack.palletsortingapi.order.domain.emuns.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.jaxb.SpringDataJaxb.OrderDto;
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
public class OrderController {

  private final OrderService orderService;

  @PostMapping("/solve/{packingType}")
  public TwoDimensionSolutionResponse solveOrder(@RequestBody SolvePackingRequest request,
      @PathVariable String packingType) {
    // Logic to solve the order goes here
    return orderService.scheduleOrder(packingType, request);
  }

  @GetMapping("/available-slots")
  public List<String> getAvailableTimeSlots(@RequestParam String date) {
    return orderService.getAvailableTimeSlots(date);
  }

  @GetMapping
  public GenericResponse getAllOrders(Pageable pageable) {
    return orderService.getAllOrders(pageable);
  }

  @GetMapping("/{orderId}")
  public GenericResponse getOrderById(@PathVariable Long orderId) {
    return orderService.getOrderById(orderId);
  }

  @GetMapping("/{orderId}/status")
  public GenericResponse GetOrderStatus(@PathVariable Long orderId) {
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
      @RequestParam(required = false) String gpsLink, @RequestParam boolean denied) {
    return ResponseEntity.ok(orderService.continueOrder(orderId, amount, gpsLink, denied));
  }

  @PatchMapping
  public ResponseEntity<GenericResponse> updateOrder(@RequestBody OrderDto orderDto) {
//    GenericResponse updatedOrder = orderService.updateOrder(orderDto);
//    return ResponseEntity.ok(updatedOrder);{
    return null;
  }


  @PostMapping(value = "/{orderId}/documents/{documentId}/upload", consumes = "multipart/form-data")
  public  ResponseEntity<GenericResponse> uploadDocument(@PathVariable Long documentId,
    @PathVariable Long orderId, @RequestParam ("file") MultipartFile file) {
    return ResponseEntity.ok(orderService.uploadDocument(documentId, orderId, file));
  }

}
