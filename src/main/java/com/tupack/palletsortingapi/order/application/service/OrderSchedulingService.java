package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.application.dto.TwoDimensionSolutionResponse;
import com.tupack.palletsortingapi.order.application.mapper.TruckMapper;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.Zone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrator service for scheduling orders.
 *
 * <p>Flow:
 * <ol>
 *   <li>Fail-fast: validate client and warehouse exist (cheap DB lookups) before any expensive work</li>
 *   <li>Resolve the delivery zone once and compute the projected delivery date</li>
 *   <li>Run the packing algorithm — includes truck locking and the single availability check</li>
 *   <li>Build and persist the order entity using the already-resolved zone</li>
 *   <li>Assign the truck (status → ASSIGNED) and initialise transport tracking</li>
 *   <li>Return the response</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSchedulingService {

  private final OrderPackingService packingService;
  private final OrderInitializationService orderInitializationService;
  private final TruckSelectionService truckSelectionService;
  private final OrderPersistenceService orderPersistenceService;
  private final TransportStatusService transportStatusService;
  private final ZoneResolverService zoneResolverService;
  private final TruckMapper truckMapper;

  @Transactional
  public TwoDimensionSolutionResponse scheduleOrder(String packingType, SolvePackingRequest request) {

    // Step 1: Fail-fast — verify client and warehouse exist before the expensive packing call
    orderInitializationService.validatePrerequisites(request);

    // Step 2: Resolve zone once and compute projected delivery date.
    // Both values are stored on the request so every downstream service reads the same data
    // without issuing additional DB queries.
    Zone zone = zoneResolverService.resolveZone(request.getToAddress());
    request.setProjectedDeliveryDate(
        request.getDeliveryDate().plusMinutes(zone.getMaxDeliveryTime()));

    // Step 3: Solve packing — internally acquires a pessimistic lock on the selected truck
    // and performs the single availability check with that lock held.
    SolutionDto solution = packingService.solvePacking(packingType, request);

    // Step 4: Build the order entity using the pre-resolved zone (no second zone DB call)
    Order order = orderInitializationService.initializeOrder(packingType, request, solution, zone);
    order.setTruck(solution.getTruck());

    // Step 5: Persist order and its cargo items
    Order savedOrder = orderPersistenceService.persistOrder(order, packingType, request);

    // Step 6: Mark truck as ASSIGNED and create the initial transport-status record
    truckSelectionService.assignTruckToOrder(solution.getTruck(), savedOrder);
    transportStatusService.initializeTransportStatus(savedOrder);

    return buildResponse(solution);
  }

  private TwoDimensionSolutionResponse buildResponse(SolutionDto solution) {
    TwoDimensionSolutionResponse response = new TwoDimensionSolutionResponse();
    response.setImageUrl(solution.getTruckDistributionImageUrl());
    response.setTruck(truckMapper.toDto(solution.getTruck()));
    return response;
  }
}
