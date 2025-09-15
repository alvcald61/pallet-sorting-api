package com.tupack.palletsortingapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "application.security.jwt")
public class JwtProperties {
    private String secret;
    private int accessTokenExpirationMinutes;
    private int refreshTokenExpirationDays;
}
