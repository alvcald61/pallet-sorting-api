package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.DocumentNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.DocumentDto;
import com.tupack.palletsortingapi.order.application.mapper.DocumentMapper;
import com.tupack.palletsortingapi.order.domain.Document;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.DocumentRepository;
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
public class DocumentService {

  private final DocumentRepository documentRepository;
  private final DocumentMapper documentMapper;

  @Transactional(readOnly = true)
  public GenericResponse getAllDocuments(Pageable pageable) {
    Page<Document> page = documentRepository.findAllByEnabled(true, pageable);
    return GenericResponse.success(page.map(documentMapper::toDto));
  }

  @Transactional(readOnly = true)
  public GenericResponse getDocumentById(Long id) {
    var document = documentRepository.findByDocumentIdAndEnabled(id, true)
        .map(documentMapper::toDto)
        .orElseThrow(() -> new DocumentNotFoundException(id));
    return GenericResponse.success(document);
  }

  public GenericResponse createDocument(DocumentDto request) {
    Document entity = documentMapper.toEntity(request);
    // Evitar que el cliente fuerce el ID en un create
    entity.setDocumentId(null);
    entity.setEnabled(true);

    Document saved = documentRepository.save(entity);
    return GenericResponse.success(documentMapper.toDto(saved));
  }

  public GenericResponse updateDocument(Long id, DocumentDto request) {
    return documentRepository.findByDocumentIdAndEnabled(id, true).map(existing -> {
      documentMapper.partialUpdate(request, existing);
      // partialUpdate must not change identity or soft-delete state
      existing.setDocumentId(id);
      existing.setEnabled(true);

      Document updated = documentRepository.save(existing);
      return GenericResponse.success(documentMapper.toDto(updated));
    }).orElseThrow(() -> new DocumentNotFoundException(id));
  }

  public GenericResponse deleteDocument(Long id) {
    Document document = documentRepository.findByDocumentIdAndEnabled(id, true)
        .orElseThrow(() -> new DocumentNotFoundException(id));
    document.setEnabled(false);
    documentRepository.save(document);
    return GenericResponse.success("Document deleted successfully");
  }
}
