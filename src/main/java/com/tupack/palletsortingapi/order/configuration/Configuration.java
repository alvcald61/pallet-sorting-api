package com.tupack.palletsortingapi.order.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class Configuration {
  // Zone lookup is handled dynamically via ZoneRepository (see ZoneResolverService).
  // The static zone map was removed to ensure changes made through the management API
  // are reflected immediately without requiring a server restart.
}
