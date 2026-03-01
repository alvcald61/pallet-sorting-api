package com.tupack.palletsortingapi.order.domain.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderDocumentId implements Serializable {

  @Column(name = "order_id")
  private Long orderId;

  @Column(name = "document_id")
  private Long documentId;

  public OrderDocumentId() {}

  public OrderDocumentId(Long orderId, Long documentId) {
    this.orderId = orderId;
    this.documentId = documentId;
  }

  // Getters and setters...

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OrderDocumentId that = (OrderDocumentId) o;
    return Objects.equals(orderId, that.orderId) && Objects.equals(documentId, that.documentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderId, documentId);
  }
}
