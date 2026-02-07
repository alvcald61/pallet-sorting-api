package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.application.dto.TwoDimensionSolutionResponse;
import com.tupack.palletsortingapi.order.application.mapper.TruckMapper;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.domain.emuns.TransportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrator service for scheduling orders. This service coordinates multiple specialized
 * services to create and schedule a complete order.
 * <p>
 * Responsibilities: - Orchestrate the order creation flow - Coordinate between specialized
 * services - Manage the transaction boundary - Build the response
 * <p>
 * This service should remain thin and focus on orchestration rather than implementation details.
 */
@Service
@RequiredArgsConstructor
public class OrderSchedulingServiceRefactored {

  // Specialized services
  private final OrderPackingService packingService;
  private final OrderInitializationService orderInitializationService;
  private final TruckSelectionService truckSelectionService;
  private final OrderPersistenceService orderPersistenceService;
  private final TransportStatusService transportStatusService;

  // Mapper
  private final TruckMapper truckMapper;

  /**
   * Main orchestration method for scheduling an order
   * <p>
   * Flow: 1. Solve packing problem (find best truck and arrangement) 2. Initialize order with all
   * required data 3. Select and validate truck availability 4. Persist order and related entities
   * 5. Assign truck and initialize transport tracking 6. Build and return response
   *
   * @param packingType Packing algorithm type (2D, 3D, BULK)
   * @param request     Order request with cargo and delivery details
   * @return Response with solution details and assigned truck
   */
  @Transactional
  public TwoDimensionSolutionResponse scheduleOrder(
      String packingType,
      SolvePackingRequest request) {

    // Step 1: Solve packing problem
    SolutionDto solution = packingService.solvePacking(packingType, request);

    // Step 2: Initialize order with all data
    Order order = orderInitializationService.initializeOrder(packingType, request, solution);

    // Step 3: Select and validate truck
    Truck truck = truckSelectionService.selectTruck(solution, order);
    truckSelectionService.validateTruckAvailability(
        truck,
        order.getPickupDate(),
        order.getProjectedDeliveryDate()
    );

    // Step 4: Set truck to order
    order.setTruck(truck);

    // Step 5: Persist order and cargo
    Order savedOrder = orderPersistenceService.persistOrder(order, packingType, request);

    // Step 6: Assign truck and initialize transport tracking
    truckSelectionService.assignTruckToOrder(truck, savedOrder);
    savedOrder.setTransportStatus(TransportStatus.TRUCK_ASSIGNED);
    transportStatusService.initializeTransportStatus(savedOrder);

    // Step 7: Build and return response
    return buildResponse(solution);
  }

  /**
   * Build response with solution details
   */
  private TwoDimensionSolutionResponse buildResponse(SolutionDto solution) {
    TwoDimensionSolutionResponse response = new TwoDimensionSolutionResponse();
    response.setImageUrl(solution.getTruckDistributionImageUrl());
    response.setTruck(truckMapper.toDto(solution.getTruck()));
    return response;
  }
}
