package com.tupack.palletsortingapi.common.config;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

  private static final String SYSTEM_AUDITOR = "SYSTEM";

  @Bean
  AuditorAware<String> auditorAware() {
    return () -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null
          || !authentication.isAuthenticated()
          || "anonymousUser".equals(authentication.getPrincipal())) {
        return Optional.of(SYSTEM_AUDITOR);
      }
      Object principal = authentication.getPrincipal();
      if (principal instanceof UserDetails userDetails) {
        return Optional.of(userDetails.getUsername());
      }
      return Optional.of(principal.toString());
    };
  }
}
