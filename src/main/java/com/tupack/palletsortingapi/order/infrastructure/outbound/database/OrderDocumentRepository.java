package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.OrderDocument;
import com.tupack.palletsortingapi.order.domain.id.OrderDocumentId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderDocumentRepository extends JpaRepository<OrderDocument, OrderDocumentId> {
  List<OrderDocument> getAllByOrder(Order order);

  @Query("select od from OrderDocument od where od.order.id = :orderId and od.document.documentId = :documentId")
  Optional<OrderDocument> getByOrderIdAndDocumentId(Long orderId, Long documentId);
}
