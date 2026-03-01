package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.OrderStatusUpdate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface OrderStatusUpdateRepository
    extends JpaRepository<OrderStatusUpdate, Long> {
  List<OrderStatusUpdate> getAllByOrder_Id(Long orderId);

  Long order(Order order);

  List<OrderStatusUpdate> getAllByOrder_IdOrderByCreatedAtDesc(Long orderId);
}
