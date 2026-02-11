package com.tupack.palletsortingapi.order.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class EstimateCostRequest {

  @NotNull(message = "Order type is required")
  private String orderType; // BULK or TWO_DIMENSIONAL

  @NotEmpty(message = "Items list cannot be empty")
  @Valid
  private List<EstimateItemDto> items;

  @NotNull(message = "From address is required")
  @Valid
  private AddressDto fromAddress;

  @NotNull(message = "To address is required")
  @Valid
  private AddressDto toAddress;

  @NotNull(message = "Pickup date is required")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private LocalDate pickupDate;

  public Double getTotalVolume() {
    return items.stream()
        .mapToDouble(item -> item.getVolume() * item.getQuantity())
        .sum();
  }

  public Double getTotalWeight() {
    return items.stream()
        .mapToDouble(item -> item.getWeight() * item.getQuantity())
        .sum();
  }
}
