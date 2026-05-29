package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.ZoneRequest;
import com.tupack.palletsortingapi.order.application.service.ZoneManagementService;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zone")
@RequiredArgsConstructor
@Slf4j
class ZoneController {

  private final ZoneManagementService zoneManagementService;

  @GetMapping
  public GenericResponse getAllZones() {
    return zoneManagementService.getAllZones();
  }

  @GetMapping("/{id}")
  public GenericResponse getZoneById(@PathVariable Long id) {
    return zoneManagementService.getZoneById(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public GenericResponse createZone(@Valid @RequestBody ZoneRequest request) {
    return zoneManagementService.createZone(request);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public GenericResponse updateZone(@PathVariable Long id, @Valid @RequestBody ZoneRequest request) {
    return zoneManagementService.updateZone(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public GenericResponse deleteZone(@PathVariable Long id) {
    return zoneManagementService.deleteZone(id);
  }
}
