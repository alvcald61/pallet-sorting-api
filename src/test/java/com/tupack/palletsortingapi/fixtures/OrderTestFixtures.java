package com.tupack.palletsortingapi.fixtures;

import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import com.tupack.palletsortingapi.utils.PackingType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Test fixtures for Order entities.
 * Provides factory methods for creating test data.
 */
public class OrderTestFixtures {

    public static Order createOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setOrderType(PackingType.TWO_DIMENSIONAL);
        order.setOrderStatus(OrderStatus.REVIEW);
        order.setPickupDate(LocalDateTime.now().plusDays(1));
        order.setProjectedDeliveryDate(LocalDateTime.now().plusDays(2));
        order.setTotalVolume(BigDecimal.valueOf(10.5));
        order.setTotalWeight(BigDecimal.valueOf(500.0));
        return order;
    }

    public static Order createOrderInProgress() {
        Order order = createOrder();
        order.setOrderStatus(OrderStatus.IN_PROGRESS);
        return order;
    }

    public static Order createOrderDelivered() {
        Order order = createOrder();
        order.setOrderStatus(OrderStatus.DELIVERED);
        return order;
    }

    public static Order createOrderWithId(Long id) {
        Order order = createOrder();
        order.setId(id);
        return order;
    }

    public static Order createOrderDenied() {
        Order order = createOrder();
        order.setOrderStatus(OrderStatus.DENIED);
        return order;
    }
}
