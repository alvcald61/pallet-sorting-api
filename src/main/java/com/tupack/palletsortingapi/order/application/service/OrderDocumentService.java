package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.common.exception.OrderDocumentNotFoundException;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.OrderDocument;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import com.tupack.palletsortingapi.order.domain.id.OrderDocumentId;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderDocumentRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.storage.LocalFileUploader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDocumentService {

  private final OrderDocumentRepository orderDocumentRepository;
  private final OrderRepository orderRepository;
  private final LocalFileUploader localFileUploader;

  public List<OrderDocument> createDocumentOrder(Order order) {
    return order.getWarehouse().getDocuments().stream().map(warehouseDocument -> new OrderDocument(
        new OrderDocumentId(order.getId(), warehouseDocument.getDocumentId()), warehouseDocument,
        order, null)).collect(Collectors.toList());
  }

  public GenericResponse uploadDocument(Long documentId, Long orderId, MultipartFile file) {
    OrderDocument orderDocument =
        orderDocumentRepository.getByOrderIdAndDocumentId(orderId, documentId)
            .orElseThrow(() -> new OrderDocumentNotFoundException(orderId, documentId));
    String fileName = orderId + "-" + documentId + "-" + file.getOriginalFilename();
    try {
      String link = localFileUploader.upload(fileName, file.getBytes());
      orderDocument.setLink(link);
      orderDocumentRepository.save(orderDocument);
      Order order = orderDocument.getOrder();
      if (order.getDocument().stream().filter(od -> od.getDocument().getRequired())
          .allMatch(doc -> doc.getLink() != null)) {
        order.setDocumentPending(false);
        order.setOrderStatus(OrderStatus.IN_PROGRESS);
        orderRepository.save(order);
      }
      return GenericResponse.success(link);
    } catch (IOException e) {
      throw new BusinessException("Failed to upload document: " + e.getMessage(),
          "DOCUMENT_UPLOAD_FAILED");
    }
  }
}
