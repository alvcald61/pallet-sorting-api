package com.tupack.palletsortingapi.order.application.dto;

import com.tupack.palletsortingapi.order.domain.enums.TransportStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transport status update information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportStatusUpdateDto {

  private Long id;
  private TransportStatus status;
  private String statusDisplayName;
  private LocalDateTime timestamp;
  private Double locationLatitude;
  private Double locationLongitude;
  private String locationAddress;
  private String notes;
  private String updatedBy;
  private String photoUrl;
  private String signatureUrl;
}
