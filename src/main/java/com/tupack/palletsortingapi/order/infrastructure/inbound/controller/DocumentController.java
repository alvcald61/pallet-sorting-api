package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.DocumentDto;
import com.tupack.palletsortingapi.order.application.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
class DocumentController {

  private final DocumentService documentService;

  /**
   * Get all documents
   */
  @GetMapping
  public ResponseEntity<GenericResponse> getAllDocuments(
      @PageableDefault(size = 1000) Pageable pageable
  ) {
    GenericResponse response = documentService.getAllDocuments(pageable);
    return ResponseEntity.ok(response);
  }

  /**
   * Get document by ID
   */
  @GetMapping("/{id}")
  public ResponseEntity<GenericResponse> getDocumentById(@PathVariable Long id) {
    GenericResponse response = documentService.getDocumentById(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Create a new document
   */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<GenericResponse> createDocument(@Valid @RequestBody DocumentDto request) {
    GenericResponse response = documentService.createDocument(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update an existing document
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<GenericResponse> updateDocument(
      @PathVariable Long id,
      @Valid @RequestBody DocumentDto request
  ) {
    GenericResponse response = documentService.updateDocument(id, request);
    return ResponseEntity.ok(response);
  }

  /**
   * Soft-delete a document
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<GenericResponse> deleteDocument(@PathVariable Long id) {
    GenericResponse response = documentService.deleteDocument(id);
    return ResponseEntity.ok(response);
  }
}
