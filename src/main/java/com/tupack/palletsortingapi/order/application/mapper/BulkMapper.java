package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.PalletBulkDto;
import com.tupack.palletsortingapi.order.application.dto.PalletDto;
import com.tupack.palletsortingapi.order.domain.Bulk;
import com.tupack.palletsortingapi.order.domain.Pallet;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface BulkMapper {
  Bulk toEntity(PalletBulkDto palletDto);

  PalletBulkDto toDto(Bulk pallet);

}