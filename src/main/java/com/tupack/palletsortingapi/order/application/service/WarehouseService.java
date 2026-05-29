package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.DocumentNotFoundException;
import com.tupack.palletsortingapi.common.exception.WarehouseNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.UpdateDocumentWarehouseDto;
import com.tupack.palletsortingapi.order.application.dto.WarehouseDto;
import com.tupack.palletsortingapi.order.application.mapper.DocumentMapper;
import com.tupack.palletsortingapi.order.application.mapper.WarehouseMapper;
import com.tupack.palletsortingapi.order.domain.Document;
import com.tupack.palletsortingapi.order.domain.Warehouse;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.DocumentRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.WarehouseRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  public GenericResponse getAllWarehouses(Pageable pageable) {
    Page<Warehouse> page = warehouseRepository.findAllByEnabled(true, pageable);
    return GenericResponse.success(page.map(warehouseMapper::toDto));
  }

  @Transactional(readOnly = true)
  public GenericResponse getWarehouseById(Long id) {
    var warehouse = warehouseRepository.findByWarehouseIdAndEnabled(id, true)
        .map(warehouseMapper::toDto)
        .orElseThrow(() -> new WarehouseNotFoundException(id));
    return GenericResponse.success(warehouse);
  }

  public GenericResponse createWarehouse(WarehouseDto request) {
    Warehouse entity = warehouseMapper.toEntity(request);
    // Evitar que el cliente fuerce el ID en un create
    entity.setWarehouseId(null);
    entity.setEnabled(true);

    Warehouse saved = warehouseRepository.save(entity);
    return GenericResponse.success(warehouseMapper.toDto(saved));
  }

  public GenericResponse updateWarehouse(Long id, WarehouseDto request) {
    return warehouseRepository.findByWarehouseIdAndEnabled(id, true).map(existing -> {
      warehouseMapper.partialUpdate(request, existing);
      // partialUpdate must not change identity or soft-delete state
      existing.setWarehouseId(id);
      existing.setEnabled(true);

      Warehouse updated = warehouseRepository.save(existing);
      return GenericResponse.success(warehouseMapper.toDto(updated));
    }).orElseThrow(() -> new WarehouseNotFoundException(id));
  }

  public GenericResponse deleteWarehouse(Long id) {
    Warehouse warehouse = warehouseRepository.findByWarehouseIdAndEnabled(id, true)
        .orElseThrow(() -> new WarehouseNotFoundException(id));
    warehouse.setEnabled(false);
    warehouseRepository.save(warehouse);
    return GenericResponse.success("Warehouse deleted successfully");
  }

  @Transactional(readOnly = true)
  public GenericResponse getWarehouseDocuments(Long warehouseId) {
    Warehouse warehouse = warehouseRepository.findByWarehouseIdAndEnabled(warehouseId, true)
        .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));

    var documents = warehouse.getDocuments().stream()
        .filter(Document::isEnabled)
        .map(documentMapper::toDto)
        .collect(Collectors.toList());

    return GenericResponse.success(documents);
  }

  public GenericResponse updateWarehouseDocuments(Long warehouseId, List<UpdateDocumentWarehouseDto> documentIds) {
    Warehouse warehouse = warehouseRepository.findByWarehouseIdAndEnabled(warehouseId, true)
        .orElseThrow(() -> new WarehouseNotFoundException(warehouseId));

    // Find all documents by IDs
    Set<Document> documents = new LinkedHashSet<>();
    for (UpdateDocumentWarehouseDto documentId : documentIds) {
      Document document = documentRepository
          .findByDocumentIdAndEnabled(documentId.documentId(), true)
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
