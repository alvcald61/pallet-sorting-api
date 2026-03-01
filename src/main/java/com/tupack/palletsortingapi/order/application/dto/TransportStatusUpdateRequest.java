package com.tupack.palletsortingapi.order.application.dto;

import com.tupack.palletsortingapi.order.domain.enums.TransportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

  @NotNull(message = "El estado del transporte es requerido")
  private TransportStatus status;

  private Double latitude;

  private Double longitude;

  @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
  private String address;

  @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
  private String notes;

  @Size(max = 255, message = "La URL de la foto no puede exceder 255 caracteres")
  private String photoUrl;
}
