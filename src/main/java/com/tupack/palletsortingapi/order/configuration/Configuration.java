package com.tupack.palletsortingapi.order.configuration;

import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.ZoneRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
@org.springframework.context.annotation.Configuration
public class Configuration {

  private final ZoneRepository zoneRepository;

  @Bean
  public Map<String, List<Zone>> zoneCostPerKmMap() {
    return zoneRepository.findAllByEnabled(true).stream().collect(
        Collectors.groupingBy(zone -> zone.getState().toLowerCase(),
            Collectors.mapping(zone -> zone, Collectors.toList())));
  }

}
