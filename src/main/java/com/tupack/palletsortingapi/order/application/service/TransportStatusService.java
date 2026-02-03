package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.common.exception.OrderNotFoundException;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.TransportStatusUpdate;
import com.tupack.palletsortingapi.order.domain.emuns.OrderStatus;
import com.tupack.palletsortingapi.order.domain.emuns.TransportStatus;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.TransportStatusUpdateRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing transport status updates with granular tracking.
 * Provides detailed tracking of cargo movement and handling operations.
 */
@Service
@RequiredArgsConstructor
public class TransportStatusService {

  private final OrderRepository orderRepository;
  private final TransportStatusUpdateRepository transportStatusUpdateRepository;

  /**
   * Update transport status with basic information
   */
  @Transactional
  public GenericResponse updateTransportStatus(Long orderId, TransportStatus newStatus) {
    return updateTransportStatus(orderId, newStatus, null, null, null, null);
  }

  /**
   * Update transport status with location and notes
   */
  @Transactional
  public GenericResponse updateTransportStatus(
      Long orderId,
      TransportStatus newStatus,
      Double latitude,
      Double longitude,
      String address,
      String notes) {

    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    // Validate order is in a state that allows transport updates
    if (order.getOrderStatus() != OrderStatus.IN_PROGRESS) {
      throw new BusinessException(
          String.format("Cannot update transport status for order in status: %s. "
              + "Order must be IN_PROGRESS", order.getOrderStatus()),
          "INVALID_ORDER_STATUS_FOR_TRANSPORT");
    }

    // Validate status transition
    TransportStatus currentStatus = order.getTransportStatus();
    if (currentStatus != null && !currentStatus.canTransitionTo(newStatus)) {
      throw new BusinessException(
          String.format("Invalid transport status transition from %s to %s",
              currentStatus, newStatus),
          "INVALID_TRANSPORT_STATUS_TRANSITION");
    }

    // Create status update record
    String updatedBy = getCurrentUsername();
    TransportStatusUpdate statusUpdate = TransportStatusUpdate.builder()
        .order(order)
        .status(newStatus)
        .timestamp(LocalDateTime.now())
        .locationLatitude(latitude)
        .locationLongitude(longitude)
        .locationAddress(address)
        .notes(notes)
        .updatedBy(updatedBy)
        .build();

    transportStatusUpdateRepository.save(statusUpdate);

    // Update order's current transport status
    order.setTransportStatus(newStatus);

    // If delivered, update order status to COMPLETED
    if (newStatus == TransportStatus.DELIVERED) {
      order.setOrderStatus(OrderStatus.DELIVERED);
      order.setRealDeliveryDate(LocalDateTime.now());
    }

    orderRepository.save(order);

    return GenericResponse.success("Transport status updated successfully to: " + newStatus.getDisplayName());
  }

  /**
   * Update transport status with photo evidence
   */
  @Transactional
  public GenericResponse updateTransportStatusWithPhoto(
      Long orderId,
      TransportStatus newStatus,
      String photoUrl,
      Double latitude,
      Double longitude,
      String notes) {

    GenericResponse response = updateTransportStatus(orderId, newStatus, latitude, longitude, null,
        notes);

    // Update the latest status update with photo
    TransportStatusUpdate latestUpdate = transportStatusUpdateRepository
        .findFirstByOrder_IdOrderByTimestampDesc(orderId)
        .orElseThrow(() -> new BusinessException("Status update not found", "STATUS_UPDATE_NOT_FOUND"));

    latestUpdate.setPhotoUrl(photoUrl);
    transportStatusUpdateRepository.save(latestUpdate);

    return response;
  }

  /**
   * Get transport status history for an order
   */
  public GenericResponse getTransportStatusHistory(Long orderId) {
    if (!orderRepository.existsById(orderId)) {
      throw new OrderNotFoundException(orderId);
    }

    List<TransportStatusUpdate> history =
        transportStatusUpdateRepository.findByOrder_IdOrderByTimestampDesc(orderId);

    return GenericResponse.success(history);
  }

  /**
   * Get current transport status for an order
   */
  public GenericResponse getCurrentTransportStatus(Long orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    TransportStatus currentStatus = order.getTransportStatus();
    if (currentStatus == null) {
      currentStatus = TransportStatus.PENDING;
    }

    return GenericResponse.success(currentStatus);
  }

  /**
   * Get transport timeline (all updates with location)
   */
  public GenericResponse getTransportTimeline(Long orderId) {
    if (!orderRepository.existsById(orderId)) {
      throw new OrderNotFoundException(orderId);
    }

    List<TransportStatusUpdate> timeline =
        transportStatusUpdateRepository.findByOrderIdWithLocation(orderId);

    return GenericResponse.success(timeline);
  }

  /**
   * Initialize transport status when order starts
   */
  @Transactional
  public void initializeTransportStatus(Order order) {
    order.setTransportStatus(TransportStatus.PENDING);
    orderRepository.save(order);

    TransportStatusUpdate initialUpdate = new TransportStatusUpdate(order,
        TransportStatus.PENDING);
    initialUpdate.setUpdatedBy("SYSTEM");
    transportStatusUpdateRepository.save(initialUpdate);
  }

  /**
   * Get current username from security context
   */
  private String getCurrentUsername() {
    try {
      return SecurityContextHolder.getContext().getAuthentication().getName();
    } catch (Exception e) {
      return "SYSTEM";
    }
  }
}
