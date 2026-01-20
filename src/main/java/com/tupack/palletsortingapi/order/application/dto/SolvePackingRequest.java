package com.tupack.palletsortingapi.order.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class SolvePackingRequest {
  private List<PalletBulkDto> pallets;
  private Double totalWeight;
  private Double totalVolume;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
  private LocalDateTime deliveryDate;
  //  private String address;
  private String city;
  private String street;
  private String zoneId;
  //  private String fromAddress;
  //  private String toAddress;
  private AddressDto fromAddress;
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
