package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.ZoneDto;
import com.tupack.palletsortingapi.order.application.service.ZoneManagementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  public GenericResponse createZone(@RequestBody ZoneDto dto) {
    return zoneManagementService.createZone(dto);
  }

  @PutMapping("/{id}")
  public GenericResponse updateZone(@PathVariable Long id, @RequestBody ZoneDto dto) {
    return zoneManagementService.updateZone(id, dto);
  }

  @DeleteMapping("/{id}")
  public GenericResponse deleteZone(@PathVariable Long id) {
    return zoneManagementService.deleteZone(id);
  }
}
