package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.TransportStatusUpdateRequest;
import com.tupack.palletsortingapi.order.application.service.TransportStatusService;
import com.tupack.palletsortingapi.order.domain.enums.TransportStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing transport status tracking.
 * Provides endpoints for updating and retrieving granular transport status information.
 */
@RestController
@RequestMapping("/api/order/{orderId}/transport")
@RequiredArgsConstructor
@Slf4j
public class TransportStatusController {

  private final TransportStatusService transportStatusService;

  /**
   * Update transport status for an order
   *
   * @param orderId Order ID
   * @param request Transport status update request with location and notes
   * @return Success response
   */
  @PatchMapping("/status")
  public ResponseEntity<GenericResponse> updateTransportStatus(
      @PathVariable Long orderId,
      @Valid @RequestBody TransportStatusUpdateRequest request) {

    GenericResponse response;
    if (request.getPhotoUrl() != null && !request.getPhotoUrl().isEmpty()) {
      response = transportStatusService.updateTransportStatusWithPhoto(
          orderId,
          request.getStatus(),
          request.getPhotoUrl(),
          request.getLatitude(),
          request.getLongitude(),
          request.getNotes()
      );
    } else {
      response = transportStatusService.updateTransportStatus(
          orderId,
          request.getStatus(),
          request.getLatitude(),
          request.getLongitude(),
          request.getAddress(),
          request.getNotes()
      );
    }

    return ResponseEntity.ok(response);
  }

  /**
   * Quick update transport status (without location/notes)
   *
   * @param orderId Order ID
   * @param status  New transport status
   * @return Success response
   */
  @PatchMapping("/status/quick")
  public ResponseEntity<GenericResponse> quickUpdateTransportStatus(
      @PathVariable Long orderId,
      @RequestParam TransportStatus status) {

    GenericResponse response = transportStatusService.updateTransportStatus(orderId, status);
    return ResponseEntity.ok(response);
  }

  /**
   * Get transport status history for an order
   *
   * @param orderId Order ID
   * @return List of transport status updates ordered by timestamp
   */
  @GetMapping("/history")
  public ResponseEntity<GenericResponse> getTransportStatusHistory(@PathVariable Long orderId) {
    GenericResponse response = transportStatusService.getTransportStatusHistory(orderId);
    return ResponseEntity.ok(response);
  }

  /**
   * Get current transport status
   *
   * @param orderId Order ID
   * @return Current transport status
   */
  @GetMapping("/status")
  public ResponseEntity<GenericResponse> getCurrentTransportStatus(@PathVariable Long orderId) {
    GenericResponse response = transportStatusService.getCurrentTransportStatus(orderId);
    return ResponseEntity.ok(response);
  }

  /**
   * Get transport timeline with location tracking
   *
   * @param orderId Order ID
   * @return List of transport updates with GPS coordinates
   */
  @GetMapping("/timeline")
  public ResponseEntity<GenericResponse> getTransportTimeline(@PathVariable Long orderId) {
    GenericResponse response = transportStatusService.getTransportTimeline(orderId);
    return ResponseEntity.ok(response);
  }
}
