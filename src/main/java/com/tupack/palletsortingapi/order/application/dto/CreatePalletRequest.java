package com.tupack.palletsortingapi.order.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a Pallet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePalletRequest implements Serializable {
  @NotBlank(message = "El tipo de pallet es requerido")
  @Size(max = 50, message = "El tipo no puede exceder 50 caracteres")
  private String type;

  @NotNull(message = "El ancho es requerido")
  @Positive(message = "El ancho debe ser positivo")
  private Double width;

  @NotNull(message = "El largo es requerido")
  @Positive(message = "El largo debe ser positivo")
  private Double length;

  @NotNull(message = "La altura es requerida")
  @Positive(message = "La altura debe ser positiva")
  private Double height;

  @NotNull(message = "La cantidad es requerida")
  @Positive(message = "La cantidad debe ser positiva")
  private Integer amount;
}

