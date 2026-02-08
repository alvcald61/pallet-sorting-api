package com.tupack.palletsortingapi.order.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class SolvePackingRequest {
  @NotEmpty(message = "La lista de pallets no puede estar vacía")
  @Valid
  private List<PalletBulkDto> pallets;

  private Double totalWeight;

  private Double totalVolume;

  @NotNull(message = "La fecha de entrega es requerida")
  @Future(message = "La fecha de entrega debe ser futura")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
  private LocalDateTime deliveryDate;

  private LocalDateTime endDate;

  @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
  private String city;

  @Size(max = 255, message = "La calle no puede exceder 255 caracteres")
  private String street;

  private String zoneId;

  @NotNull(message = "La dirección de origen es requerida")
  @Valid
  private AddressDto fromAddress;

  @NotNull(message = "La dirección de destino es requerida")
  @Valid
  private AddressDto toAddress;

  private String userId;

  public Double getTotalWeight() {
    if (totalWeight == null) {
      totalWeight =
          pallets.stream().mapToDouble(data -> data.getWeight() * data.getQuantity()).sum();
    }
    return totalWeight;
  }

  public Double getTotalVolume() {
    if (totalVolume == null) {
      totalVolume = pallets.stream().mapToDouble(
              (data -> data.getVolume() != null ? data.getVolume() :
                data.getHeight() * data.getWidth() * data.getLength() * data.getQuantity()))
          .sum();
    }
    return totalVolume;
  }
}
