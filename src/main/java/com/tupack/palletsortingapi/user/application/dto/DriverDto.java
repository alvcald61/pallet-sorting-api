package com.tupack.palletsortingapi.user.application.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.tupack.palletsortingapi.user.domain.Driver}
 */
public record DriverDto(Long driverId, LocalDateTime createdAt, LocalDateTime updatedAt,
    String createdBy, String updatedBy, boolean enabled, String dni, String phone)
    implements Serializable {
}