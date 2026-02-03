package com.tupack.palletsortingapi.order.application.dto;

import com.tupack.palletsortingapi.order.domain.emuns.TransportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating transport status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportStatusUpdateRequest {

  @NotNull(message = "Transport status is required")
  private TransportStatus status;

  private Double latitude;

  private Double longitude;

  private String address;

  private String notes;

  private String photoUrl;
}
