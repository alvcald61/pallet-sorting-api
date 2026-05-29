package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.ZoneNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.ZoneDto;
import com.tupack.palletsortingapi.order.application.dto.ZoneRequest;
import com.tupack.palletsortingapi.order.application.mapper.ZoneDtoMapper;
import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.ZoneRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneManagementService {

  private final ZoneRepository zoneRepository;
  private final ZoneDtoMapper zoneDtoMapper;

  public GenericResponse getAllZones() {
    List<ZoneDto> zones = zoneRepository.findAllByEnabled(true)
        .stream()
        .map(zoneDtoMapper::toDto)
        .toList();
    return GenericResponse.success(zones);
  }

  public GenericResponse getZoneById(Long id) {
    Zone zone = zoneRepository.findById(id)
        .orElseThrow(() -> new ZoneNotFoundException(id));
    return GenericResponse.success(zoneDtoMapper.toDto(zone));
  }

  @Transactional
  public GenericResponse createZone(ZoneRequest request) {
    Zone zone = zoneDtoMapper.toEntity(request);
    zone.setEnabled(true);
    zone = zoneRepository.save(zone);
    return GenericResponse.success(zoneDtoMapper.toDto(zone));
  }

  @Transactional
  public GenericResponse updateZone(Long id, ZoneRequest request) {
    Zone zone = zoneRepository.findById(id)
        .orElseThrow(() -> new ZoneNotFoundException(id));
    zoneDtoMapper.partialUpdate(request, zone);
    zone = zoneRepository.save(zone);
    return GenericResponse.success(zoneDtoMapper.toDto(zone));
  }

  @Transactional
  public GenericResponse deleteZone(Long id) {
    Zone zone = zoneRepository.findById(id)
        .orElseThrow(() -> new ZoneNotFoundException(id));
    zone.setEnabled(false);
    zoneRepository.save(zone);
    return GenericResponse.success(null, "Zona deshabilitada correctamente");
  }
}
