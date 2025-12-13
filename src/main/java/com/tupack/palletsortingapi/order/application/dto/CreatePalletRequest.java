package com.tupack.palletsortingapi.order.application.dto;

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
  private String type;
  private Double width;
  private Double length;
  private Double height;
  private Integer amount;
}

