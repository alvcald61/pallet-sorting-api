package com.tupack.palletsortingapi.user.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.user.application.DriverService;
import com.tupack.palletsortingapi.user.application.dto.CreateDriverRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/driver")
@RestController
@RequiredArgsConstructor
public class DriverController {

  private final DriverService driverService;

  /**
   * Get all active drivers
   *
   * @return GenericResponse with list of all active drivers
   */
  @GetMapping
  public ResponseEntity<GenericResponse> getAllDrivers() {
    GenericResponse response = driverService.getAllDrivers();
    return ResponseEntity.ok(response);
  }

  /**
   * Get a driver by ID
   *
   * @param id the driver ID
   * @return GenericResponse with driver details
   */
  @GetMapping("/{id}")
  public ResponseEntity<GenericResponse> getDriverById(@PathVariable Long id) {
    GenericResponse response = driverService.getDriverById(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Create a new driver and associated user
   *
   * @param request the driver creation request with user details
   * @return GenericResponse with created driver
   */
  @PostMapping
  public ResponseEntity<GenericResponse> createDriver(@RequestBody CreateDriverRequest request) {
    GenericResponse response = driverService.createDriver(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Update an existing driver
   *
   * @param id the driver ID
   * @param request the driver update request
   * @return GenericResponse with updated driver
   */
  @PutMapping("/{id}")
  public ResponseEntity<GenericResponse> updateDriver(@PathVariable Long id,
      @RequestBody CreateDriverRequest request) {
    GenericResponse response = driverService.updateDriver(id, request);
    return ResponseEntity.ok(response);
  }

  /**
   * Delete (soft delete) a driver
   *
   * @param id the driver ID
   * @return GenericResponse with success message
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<GenericResponse> deleteDriver(@PathVariable Long id) {
    GenericResponse response = driverService.deleteDriver(id);
    return ResponseEntity.ok(response);
  }
}

