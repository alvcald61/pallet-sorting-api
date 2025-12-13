package com.tupack.palletsortingapi.order.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class PalletBulkDto {
  private String type;
  //caso Bulk
  private Double volume; // caso Bulk
  // caso 2D
  @JsonProperty("id")
  private Long palletId;
  private Double weight;
  private Double length;
  private Double width;
  // caso 3D
  private Double height;
  // todos
  private Integer quantity;

}
