package com.tupack.palletsortingapi.order.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class SolvePackingRequest {
  @NotEmpty(message = "Pallets list cannot be empty")
  @Valid
  private List<PalletBulkDto> pallets;
  private Double totalWeight;
  private Double totalVolume;
  @NotNull(message = "Delivery date is required")
  @Future(message = "Delivery date must be in the future")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
  private LocalDateTime deliveryDate;
  private LocalDateTime endDate;
  //  private String address;
  private String city;
  private String street;
  private String zoneId;
  //  private String fromAddress;
  //  private String toAddress;
  @NotNull(message = "From address is required")
  @Valid
  private AddressDto fromAddress;
  @NotNull(message = "To address is required")
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
