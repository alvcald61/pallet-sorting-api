package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.order.application.TruckService;
import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.TruckDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/truck")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TruckController {

  private final TruckService truckService;

  /**
   * Get all trucks
   *
   * @return GenericResponse with list of all active trucks
   */
  @GetMapping
  public ResponseEntity<GenericResponse> getAllTrucks() {
    GenericResponse response = truckService.getAllTrucks();
    return ResponseEntity.ok(response);
  }

  /**
   * Get truck by ID
   *
   * @param id the truck ID
   * @return GenericResponse with truck details
   */
  @GetMapping("/{id}")
  public ResponseEntity<GenericResponse> getTruckById(@PathVariable Long id) {
    GenericResponse response = truckService.getTruckById(id);
    return ResponseEntity.ok(response);

  }

  /**
   * Create a new truck
   *
   * @param truckDto the truck data transfer object
   * @return GenericResponse with created truck
   */
  @PostMapping
  public ResponseEntity<GenericResponse> createTruck(@RequestBody TruckDto truckDto) {
    GenericResponse response = truckService.createTruck(truckDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update an existing truck
   *
   * @param id the truck ID
   * @param truckDto the truck data transfer object with updates
   * @return GenericResponse with updated truck
   */
  @PutMapping("/{id}")
  public ResponseEntity<GenericResponse> updateTruck(@PathVariable Long id,
      @RequestBody TruckDto truckDto) {
    GenericResponse response = truckService.updateTruck(id, truckDto);
      return ResponseEntity.ok(response);
  }

  /**
   * Delete (soft delete) a truck
   *
   * @param id the truck ID
   * @return GenericResponse with success message
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<GenericResponse> deleteTruck(@PathVariable Long id) {
    GenericResponse response = truckService.deleteTruck(id);
      return ResponseEntity.ok(response);
  }
}

