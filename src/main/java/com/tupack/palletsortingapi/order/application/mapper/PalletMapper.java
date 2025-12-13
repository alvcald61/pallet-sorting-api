package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.CreatePalletRequest;
import com.tupack.palletsortingapi.order.application.dto.PalletDto;
import com.tupack.palletsortingapi.order.domain.Pallet;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface PalletMapper {
  Pallet toEntity(PalletDto palletDto);

  Pallet toEntity(CreatePalletRequest request);

  PalletDto toDto(Pallet pallet);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Pallet partialUpdate(PalletDto palletDto, @MappingTarget Pallet pallet);

  void updateEntity(CreatePalletRequest request, @MappingTarget Pallet pallet);
}