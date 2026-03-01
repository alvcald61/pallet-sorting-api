package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.DocumentNotFoundException;
import com.tupack.palletsortingapi.order.application.mapper.DocumentMapper;
import com.tupack.palletsortingapi.order.domain.Document;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.DocumentRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  public GenericResponse getAllDocuments() {
    var documents = documentRepository.findAll().stream().map(documentMapper::toDto)
      .collect(Collectors.toList());
    return GenericResponse.success(documents);
  }

  @Transactional(readOnly = true)
  public GenericResponse getDocumentById(Long id) {
    var document = documentRepository.findById(id).map(documentMapper::toDto)
        .orElseThrow(() -> new DocumentNotFoundException(id));
    return GenericResponse.success(document);
  }

  public GenericResponse createDocument(Document request) {
    // Evitar que el cliente fuerce el ID en un create
    request.setDocumentId(null);

    Document saved = documentRepository.save(request);
    return GenericResponse.success(documentMapper.toDto(saved));
  }

  public GenericResponse updateDocument(Long id, Document request) {
    return documentRepository.findById(id).map(existing -> {
      existing.setDocumentName(request.getDocumentName());
      existing.setRequired(request.getRequired());

      Document updated = documentRepository.save(existing);
      return GenericResponse.success(documentMapper.toDto(updated));
    }).orElseThrow(() -> new DocumentNotFoundException(id));
  }

  public GenericResponse deleteDocument(Long id) {
    if (!documentRepository.existsById(id)) {
      throw new DocumentNotFoundException(id);
    }
    documentRepository.deleteById(id);
    return GenericResponse.success("Document deleted successfully");
  }
}
