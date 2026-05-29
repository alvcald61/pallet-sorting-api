package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.DocumentWarehouseDto;
import com.tupack.palletsortingapi.order.application.dto.WarehouseDto;
import com.tupack.palletsortingapi.order.application.service.WarehouseService;
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
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
@Slf4j
class WarehouseController {

  private final WarehouseService warehouseService;

  /**
   * Get all warehouses
   */
  @GetMapping
  public ResponseEntity<GenericResponse> getAllWarehouses(
      @PageableDefault(size = 1000) Pageable pageable
  ) {
    GenericResponse response = warehouseService.getAllWarehouses(pageable);
    return ResponseEntity.ok(response);
  }

  /**
   * Get warehouse by ID
   */
  @GetMapping("/{id}")
  public ResponseEntity<GenericResponse> getWarehouseById(@PathVariable Long id) {
    GenericResponse response = warehouseService.getWarehouseById(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Create a new warehouse
   */
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<GenericResponse> createWarehouse(@Valid @RequestBody WarehouseDto request) {
    GenericResponse response = warehouseService.createWarehouse(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update an existing warehouse
   */
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<GenericResponse> updateWarehouse(
      @PathVariable Long id,
      @Valid @RequestBody WarehouseDto request
  ) {
    GenericResponse response = warehouseService.updateWarehouse(id, request);
    return ResponseEntity.ok(response);
  }

  /**
   * Soft-delete a warehouse
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<GenericResponse> deleteWarehouse(@PathVariable Long id) {
    GenericResponse response = warehouseService.deleteWarehouse(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Get all documents associated with a warehouse
   */
  @GetMapping("/{id}/documents")
  public ResponseEntity<GenericResponse> getWarehouseDocuments(@PathVariable Long id) {
    GenericResponse response = warehouseService.getWarehouseDocuments(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Update documents associated with a warehouse
   */
  @PutMapping("/{id}/documents")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<GenericResponse> updateWarehouseDocuments(
      @PathVariable Long id,
      @Valid @RequestBody DocumentWarehouseDto documentIds
  ) {
    GenericResponse response = warehouseService.updateWarehouseDocuments(id, documentIds.documents());
    return ResponseEntity.ok(response);
  }
}
