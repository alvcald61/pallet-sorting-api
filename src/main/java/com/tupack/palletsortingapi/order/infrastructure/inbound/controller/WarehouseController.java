package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.order.application.WarehouseService;
import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.domain.Warehouse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WarehouseController {

  private final WarehouseService warehouseService;

  /**
   * Get all warehouses
   */
  @GetMapping
  public ResponseEntity<GenericResponse> getAllWarehouses() {
    GenericResponse response = warehouseService.getAllWarehouses();
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
  public ResponseEntity<GenericResponse> createWarehouse(@RequestBody Warehouse request) {
    GenericResponse response = warehouseService.createWarehouse(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update an existing warehouse
   */
  @PutMapping("/{id}")
  public ResponseEntity<GenericResponse> updateWarehouse(
      @PathVariable Long id,
      @RequestBody Warehouse request
  ) {
    GenericResponse response = warehouseService.updateWarehouse(id, request);
    return ResponseEntity.ok(response);
  }

  /**
   * Delete a warehouse (hard delete)
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<GenericResponse> deleteWarehouse(@PathVariable Long id) {
    GenericResponse response = warehouseService.deleteWarehouse(id);
    return ResponseEntity.ok(response);
  }
}
