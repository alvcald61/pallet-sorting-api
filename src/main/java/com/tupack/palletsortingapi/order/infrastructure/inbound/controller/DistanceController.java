package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.DistanceCalculationRequest;
import com.tupack.palletsortingapi.order.application.dto.DistanceCalculationResponse;
import com.tupack.palletsortingapi.order.application.service.DistanceCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/distance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Distance", description = "Distance calculation and routing endpoints")
public class DistanceController {

  private final DistanceCalculationService distanceCalculationService;

  @Operation(
      summary = "Calculate distance",
      description = "Calculates distance and estimated travel time between two addresses. Prepared for Google Maps Distance Matrix API integration."
  )
  @PostMapping("/calculate")
  public ResponseEntity<GenericResponse> calculateDistance(
      @Valid @RequestBody DistanceCalculationRequest request) {
    DistanceCalculationResponse distance = distanceCalculationService.calculateDistance(request);
    return ResponseEntity.ok(GenericResponse.builder()
        .message("OK").statusCode(200)
        .data(distance)
        .build());
  }
}
