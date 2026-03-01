package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.InvalidOrderStateException;
import com.tupack.palletsortingapi.common.exception.OrderNotFoundException;
import com.tupack.palletsortingapi.notification.domain.event.OrderStatusChangedEvent;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.OrderStatusUpdate;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderStatusUpdateRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusService {

  private final OrderRepository orderRepository;
  private final OrderStatusUpdateRepository orderStatusUpdateRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public GenericResponse updateOrderStatus(Long orderId, String status) {
    OrderStatus statusEnum = OrderStatus.valueOf(status);
    Order order = orderRepository.getOrderById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    if (order.getOrderStatus().equals(OrderStatus.DELIVERED) || order.getOrderStatus()
        .equals(OrderStatus.DENIED)) {
      throw new InvalidOrderStateException(order.getOrderStatus());
    }
    OrderStatus oldStatus = order.getOrderStatus();
    order.setOrderStatus(statusEnum);
    orderRepository.save(order);
    recordStatus(order);

    // Publish OrderStatusChangedEvent
    eventPublisher.publishEvent(new OrderStatusChangedEvent(this, order, oldStatus, statusEnum));
    log.info("Published OrderStatusChangedEvent for order: {} from {} to {}", orderId, oldStatus, statusEnum);

    return GenericResponse.success("Order status updated successfully");
  }

  @Transactional
  public GenericResponse continueOrder(Long orderId, BigDecimal amount, String gpsLink,
      boolean denied) {
    Order order = orderRepository.getOrderById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    OrderStatus previousStatus = order.getOrderStatus();
    switch (order.getOrderStatus()) {
      case REVIEW, PRE_APPROVED:
        updateInitialStatus(amount, order);
        if (order.getOrderStatus().equals(OrderStatus.APPROVED) && order.isDocumentPending()) {
          order.setOrderStatus(OrderStatus.DOCUMENT_PENDING);
        }
        break;
      case APPROVED:
        if (order.isDocumentPending()) {
          order.setOrderStatus(OrderStatus.DOCUMENT_PENDING);
        } else {
          order.setOrderStatus(OrderStatus.IN_PROGRESS);
          order.setGpsLink(gpsLink);
        }
        break;
      case DOCUMENT_PENDING:
        if (order.isDocumentPending()) {
          throw new InvalidOrderStateException("Cannot continue order: documents are still pending");
        }
        order.setOrderStatus(OrderStatus.IN_PROGRESS);
        if (gpsLink != null) {
          order.setGpsLink(gpsLink);
        }
        break;
      case IN_PROGRESS:
        if (gpsLink != null) {
          order.setGpsLink(gpsLink);
        }
        break;
      default:
        throw new InvalidOrderStateException(order.getOrderStatus());
    }
    if (denied) {
      order.setOrderStatus(OrderStatus.DENIED);
    }
    orderRepository.save(order);
    if (!previousStatus.equals(order.getOrderStatus())) {
      recordStatus(order);

      // Publish OrderStatusChangedEvent
      eventPublisher.publishEvent(new OrderStatusChangedEvent(this, order, previousStatus, order.getOrderStatus()));
      log.info("Published OrderStatusChangedEvent for order: {} from {} to {}", orderId, previousStatus, order.getOrderStatus());
    }
    return GenericResponse.success("Order status updated successfully");
  }

  public void recordStatus(Order order) {
    orderStatusUpdateRepository.save(new OrderStatusUpdate(order, order.getOrderStatus()));
  }

  private static void updateInitialStatus(BigDecimal amount, Order order) {
    if (amount != null) {
      order.setAmount(amount);
      order.setOrderStatus(OrderStatus.PRE_APPROVED);
      return;
    }
    order.setOrderStatus(OrderStatus.APPROVED);
  }
}
