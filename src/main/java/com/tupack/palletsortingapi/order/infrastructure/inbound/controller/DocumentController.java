package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.order.application.DocumentService;
import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.domain.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DocumentController {

  private final DocumentService documentService;

  /**
   * Get all documents
   */
  @GetMapping
  public ResponseEntity<GenericResponse> getAllDocuments() {
    GenericResponse response = documentService.getAllDocuments();
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
  public ResponseEntity<GenericResponse> createDocument(@RequestBody Document request) {
    GenericResponse response = documentService.createDocument(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update an existing document
   */
  @PutMapping("/{id}")
  public ResponseEntity<GenericResponse> updateDocument(
      @PathVariable Long id,
      @RequestBody Document request
  ) {
    GenericResponse response = documentService.updateDocument(id, request);
    return ResponseEntity.ok(response);
  }

  /**
   * Delete a document (hard delete)
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<GenericResponse> deleteDocument(@PathVariable Long id) {
    GenericResponse response = documentService.deleteDocument(id);
    return ResponseEntity.ok(response);
  }
}
