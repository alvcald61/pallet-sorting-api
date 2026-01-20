package com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase;

import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.OrderDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDocumentRepository extends JpaRepository<OrderDocument, Long> {
  List<OrderDocument> getAllByOrder(Order order);
}