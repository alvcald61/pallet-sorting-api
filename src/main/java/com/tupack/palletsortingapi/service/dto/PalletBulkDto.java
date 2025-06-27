package com.tupack.palletsortingapi.service.dto;

import lombok.Data;

@Data
public class PalletBulkDto {
  private String type;
  //caso Bulk
  private Double volume; // caso Bulk
  // caso 2D
  private Double weight;
  private Double length;
  private Double width;
  // caso 3D
  private Double height;
  // todos
  private Integer quantity;

}
