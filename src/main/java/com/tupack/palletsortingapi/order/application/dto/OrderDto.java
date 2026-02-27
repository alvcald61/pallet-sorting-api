package com.tupack.palletsortingapi.order.application.dto;

import com.tupack.palletsortingapi.order.domain.enums.TransportStatus;
import com.tupack.palletsortingapi.user.application.dto.DriverDto;
import com.tupack.palletsortingapi.user.domain.Driver;
import com.tupack.palletsortingapi.utils.PackingType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * DTO for {@link com.tupack.palletsortingapi.order.domain.Order}
 */
@Data
public class OrderDto implements Serializable {
  String id;
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  String createdBy;
  String updatedBy;
  boolean enabled;
  LocalDateTime pickupDate;
  String fromAddress;
  String toAddress;
  LocalDateTime projectedDeliveryDate;
  LocalDateTime realDeliveryDate;
  BigDecimal totalVolume;
  BigDecimal totalWeight;
  PackingType orderType;
  BigDecimal amount;
  String solutionImageUrl;
  String solution;
  String orderStatus;
  List<PalletBulkDto> packages;
  TruckDto truck;
  DriverDto driver;
  private String gpsLink;
  private String toAddressLink;
  private String fromAddressLink;
  private List<DocumentDto> documents;
  private boolean isDocumentPending = true;
  private String transportStatus;
  private String sunatDocumentPath;

}
