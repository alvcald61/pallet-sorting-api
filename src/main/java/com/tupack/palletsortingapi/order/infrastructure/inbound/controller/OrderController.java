package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.order.application.OrderService;
import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.CreateOrderTemplateRequest;
import com.tupack.palletsortingapi.order.application.dto.EstimateCostRequest;
import com.tupack.palletsortingapi.order.application.dto.EstimateCostResponse;
import com.tupack.palletsortingapi.order.application.dto.OrderAnalysisRequest;
import com.tupack.palletsortingapi.order.application.dto.OrderAnalysisResponse;
import com.tupack.palletsortingapi.order.application.dto.OrderSuggestionsResponse;
import com.tupack.palletsortingapi.order.application.dto.OrderTemplateDto;
import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.application.dto.TwoDimensionSolutionResponse;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Tag(name = "Orders", description = "Order management and pallet scheduling endpoints")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Schedule a new order", description = "Creates and schedules a new pallet order by solving the packing problem and assigning a truck")
    @PostMapping("/solve/{packingType}")
    public TwoDimensionSolutionResponse solveOrder(@Valid @RequestBody SolvePackingRequest request,
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
    public GenericResponse getAllOrders(Pageable pageable,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) List<String> status,
        @RequestParam(required = false) String orderType,
        @RequestParam(required = false) String pickupDateFrom,
        @RequestParam(required = false) String pickupDateTo) {
        return orderService.getAllOrders(pageable, search, status, orderType, pickupDateFrom, pickupDateTo);
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
        @RequestParam(required = false) String gpsLink,
        @RequestParam(required = false) boolean denied) {
        return ResponseEntity.ok(orderService.continueOrder(orderId, amount, gpsLink, denied));
    }

    @PostMapping(value = "/{orderId}/documents/{documentId}/upload", consumes = "multipart/form-data")
    public ResponseEntity<GenericResponse> uploadDocument(@PathVariable Long documentId,
        @PathVariable Long orderId, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(orderService.uploadDocument(documentId, orderId, file));
    }

    @Operation(summary = "Estimate order cost", description = "Calculates the estimated cost of an order based on volume, weight, distance, and urgency")
    @PostMapping("/estimate-cost")
    public ResponseEntity<GenericResponse> estimateCost(
        @Valid @RequestBody EstimateCostRequest request) {
        EstimateCostResponse estimate = orderService.estimateCost(request);
        return ResponseEntity.ok(
            GenericResponse.builder().message("OK").statusCode(200).data(estimate).build());
    }

    @Operation(summary = "Get order suggestions", description = "Retrieves order suggestions based on user's previous orders, including frequent items and routes")
    @GetMapping("/suggestions")
    public ResponseEntity<GenericResponse> getSuggestions(
        @Parameter(description = "User ID") @RequestParam Long userId,
        @Parameter(description = "Maximum number of suggestions per category (default: 5)")
        @RequestParam(required = false, defaultValue = "5") Integer limit) {
        OrderSuggestionsResponse suggestions = orderService.getSuggestions(userId, limit);
        return ResponseEntity.ok(
            GenericResponse.builder().message("OK").statusCode(200).data(suggestions).build());
    }

    @Operation(summary = "Create order template", description = "Saves an order configuration as a reusable template")
    @PostMapping("/template")
    public ResponseEntity<GenericResponse> createTemplate(
        @Valid @RequestBody CreateOrderTemplateRequest request,
        @Parameter(description = "User ID") @RequestParam Long userId) {
        String templateId = orderService.createTemplate(request, userId);
        return ResponseEntity.ok(GenericResponse.builder().message("OK").statusCode(200).data(
                java.util.Map.of("templateId", templateId, "message", "Plantilla creada exitosamente"))
            .build());
    }

    @Operation(summary = "Get user templates", description = "Retrieves all order templates for a specific user")
    @GetMapping("/templates")
    public ResponseEntity<GenericResponse> getUserTemplates(
        @Parameter(description = "User ID") @RequestParam Long userId) {
        List<OrderTemplateDto> templates = orderService.getUserTemplates(userId);
        return ResponseEntity.ok(
            GenericResponse.builder().message("OK").statusCode(200).data(templates).build());
    }

    @Operation(summary = "Analyze order", description = "Analyzes an order and provides optimization suggestions, warnings, and utilization metrics")
    @PostMapping("/analyze")
    public ResponseEntity<GenericResponse> analyzeOrder(
        @Valid @RequestBody OrderAnalysisRequest request) {
        OrderAnalysisResponse analysis = orderService.analyzeOrder(request);
        return ResponseEntity.ok(
            GenericResponse.builder().message("OK").statusCode(200).data(analysis).build());
    }

}
