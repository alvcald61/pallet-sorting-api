package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.exception.ZoneNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.AddressDto;
import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.ZoneRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for resolving zones based on address information.
 * Centralizes zone lookup logic to avoid duplication.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneResolverService {

  private final ZoneRepository zoneRepository;
  private final Map<String, List<Zone>> zoneMap;

  /**
   * Resolve zone from address DTO
   *
   * @param addressDto Address information
   * @return Resolved zone
   * @throws ZoneNotFoundException if no zone found for the address
   */
  public Zone resolveZone(AddressDto addressDto) {
    List<Zone> stateZones = zoneMap.get(addressDto.state().toLowerCase());

    if (stateZones == null || stateZones.isEmpty()) {
      throw new ZoneNotFoundException("No zone found for state: " + addressDto.state());
    }

    List<Zone> cityZones = stateZones.stream()
        .filter(zone -> zone.getCity().equalsIgnoreCase(addressDto.city()))
        .toList();

    if (cityZones.isEmpty()) {
      throw new ZoneNotFoundException(
          "No zone found for city: " + addressDto.city() + " in state: " + addressDto.state());
    }

    // Try to find exact district match
    return cityZones.stream()
        .filter(zone -> hasDistrict(zone, addressDto))
        .findFirst()
        .orElseGet(() -> cityZones.stream()
            .filter(zone -> zone.getDistrict().equalsIgnoreCase("*"))
            .findFirst()
            .orElseThrow(() -> new ZoneNotFoundException(
                "No zone found for district: " + addressDto.district() + " in city: " + addressDto
                    .city())));
  }

  /**
   * Resolve zone by district name (for Lima specific logic)
   *
   * @param district District name
   * @return Resolved zone
   */
  public Zone resolveZoneByDistrict(String district) {
    return zoneRepository.findZoneByDistrictContainingIgnoreCase(district)
        .orElseThrow(() -> new ZoneNotFoundException("No zone found for district: " + district));
  }

  /**
   * Check if a zone covers the specified district
   */
  private boolean hasDistrict(Zone zone, AddressDto addressDto) {
    String[] districts = zone.getDistrict().split(",");
    for (String district : districts) {
      if (district.trim().equalsIgnoreCase(addressDto.district().trim())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Format address DTO to string
   */
  public String formatAddress(AddressDto addressDto) {
    return String.format("%s, %s, %s, %s",
        addressDto.address(),
        addressDto.district(),
        addressDto.city(),
        addressDto.state());
  }
}
