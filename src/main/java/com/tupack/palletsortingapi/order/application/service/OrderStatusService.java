package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.InvalidOrderStateException;
import com.tupack.palletsortingapi.common.exception.OrderNotFoundException;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.OrderStatusUpdate;
import com.tupack.palletsortingapi.order.domain.emuns.OrderStatus;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderStatusUpdateRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderStatusService {

  private final OrderRepository orderRepository;
  private final OrderStatusUpdateRepository orderStatusUpdateRepository;

  public GenericResponse updateOrderStatus(Long orderId, String status) {
    OrderStatus statusEnum = OrderStatus.valueOf(status);
    Order order = orderRepository.getOrderById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    if (order.getOrderStatus().equals(OrderStatus.DELIVERED) || order.getOrderStatus()
        .equals(OrderStatus.DENIED)) {
      throw new InvalidOrderStateException(order.getOrderStatus());
    }
    order.setOrderStatus(statusEnum);
    orderRepository.save(order);
    recordStatus(order);
    return GenericResponse.success("Order status updated successfully");
  }

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
      default:
        throw new InvalidOrderStateException(order.getOrderStatus());
    }
    if (denied) {
      order.setOrderStatus(OrderStatus.DENIED);
    }
    orderRepository.save(order);
    if (!previousStatus.equals(order.getOrderStatus())) {
      recordStatus(order);
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
