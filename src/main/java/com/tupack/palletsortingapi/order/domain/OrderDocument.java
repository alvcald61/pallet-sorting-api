package com.tupack.palletsortingapi.order.domain;

import com.tupack.palletsortingapi.order.domain.id.OrderDocumentId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "order_document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDocument {
  @EmbeddedId
  private OrderDocumentId id = new OrderDocumentId();

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("documentId")
  @JoinColumn(name = "document_id")
  private Document document;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("orderId")
  @JoinColumn(name = "order_id")
  private Order order;

  private String link;
}
