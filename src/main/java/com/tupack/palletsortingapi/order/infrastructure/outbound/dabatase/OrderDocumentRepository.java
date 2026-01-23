package com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase;

import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.OrderDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderDocumentRepository extends JpaRepository<OrderDocument, Long> {
  List<OrderDocument> getAllByOrder(Order order);

  @Query("select od from OrderDocument od where od.order.id = :orderId and od.document.documentId = :documentId")
  Optional<OrderDocument> getByOrderIdAndDocumentId(Long orderId, Long documentId);
}