package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.DocumentNotFoundException;
import com.tupack.palletsortingapi.common.exception.WarehouseNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.UpdateDocumentWarehouseDto;
import com.tupack.palletsortingapi.order.application.mapper.DocumentMapper;
import com.tupack.palletsortingapi.order.application.mapper.WarehouseMapper;
import com.tupack.palletsortingapi.order.domain.Document;
import com.tupack.palletsortingapi.order.domain.Warehouse;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.DocumentRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.WarehouseRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WarehouseService {

  private final WarehouseRepository warehouseRepository;
  private final WarehouseMapper warehouseMapper;
  private final DocumentRepository documentRepository;
  private final DocumentMapper documentMapper;

  @Transactional(readOnly = true)
  public GenericResponse getAllWarehouses() {
    var warehouses = warehouseRepository.findAll().stream().map(warehouseMapper::toDto)
      .collect(Collectors.toList());
    return GenericResponse.success(warehouses);
  }

  @Transactional(readOnly = true)
  public GenericResponse getWarehouseById(Long id) {
    var warehouse = warehouseRepository.findById(id).map(warehouseMapper::toDto)
        .orElseThrow(() -> new WarehouseNotFoundException(id));
    return GenericResponse.success(warehouse);
  }

  public GenericResponse createWarehouse(Warehouse request) {
    // Evitar que el cliente fuerce el ID en un create
    request.setWarehouseId(null);

    Warehouse saved = warehouseRepository.save(request);
    return GenericResponse.success(warehouseMapper.toDto(saved));
  }

  public GenericResponse updateWarehouse(Long id, Warehouse request) {
    return warehouseRepository.findById(id).map(existing -> {
      existing.setName(request.getName());
      existing.setAddress(request.getAddress());
      existing.setPhone(request.getPhone());

      Warehouse updated = warehouseRepository.save(existing);
      return GenericResponse.success(warehouseMapper.toDto(updated));
    }).orElseThrow(() -> new WarehouseNotFoundException(id));
  }

  public GenericResponse deleteWarehouse(Long id) {
    if (!warehouseRepository.existsById(id)) {
      throw new WarehouseNotFoundException(id);
    }
    warehouseRepository.deleteById(id);
    return GenericResponse.success("Warehouse deleted successfully");
  }

  @Transactional(readOnly = true)
  public GenericResponse getWarehouseDocuments(Long warehouseId) {
    Warehouse warehouse = warehouseRepository.findById(warehouseId)
        .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));

    var documents = warehouse.getDocuments().stream()
        .map(documentMapper::toDto)
        .collect(Collectors.toList());

    return GenericResponse.success(documents);
  }

  public GenericResponse updateWarehouseDocuments(Long warehouseId, List<UpdateDocumentWarehouseDto> documentIds) {
    Warehouse warehouse = warehouseRepository.findById(warehouseId)
        .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));

    // Find all documents by IDs
    Set<Document> documents = new LinkedHashSet<>();
    for (UpdateDocumentWarehouseDto documentId : documentIds) {
      Document document = documentRepository.findById(documentId.documentId())
          .orElseThrow(() -> new DocumentNotFoundException(documentId.documentId()));
      documents.add(document);
    }

    // Update the warehouse's documents
    warehouse.setDocuments(documents);
    Warehouse updated = warehouseRepository.save(warehouse);

    var documentDtos = updated.getDocuments().stream()
        .map(documentMapper::toDto)
        .collect(Collectors.toList());

    return GenericResponse.success(documentDtos);
  }
}
