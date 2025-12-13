package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.PalletBulkDto;
import com.tupack.palletsortingapi.order.domain.Bulk;
import com.tupack.palletsortingapi.order.domain.OrderPallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface OrderPalletMapper {
  OrderPallet toEntity(PalletBulkDto palletDto);

  @Mapping(source = "pallet.width", target = "width")
  @Mapping(source = "pallet.length", target = "length")
  @Mapping(source = "pallet.id", target = "palletId")
  @Mapping(source = "pallet.height", target = "height")
  PalletBulkDto toDto(OrderPallet pallet);

}