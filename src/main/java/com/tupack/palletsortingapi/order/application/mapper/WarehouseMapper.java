package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.WarehouseDto;
import com.tupack.palletsortingapi.order.domain.Warehouse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface WarehouseMapper {
  Warehouse toEntity(WarehouseDto warehouseDto);

  WarehouseDto toDto(Warehouse warehouse);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Warehouse partialUpdate(WarehouseDto warehouseDto, @MappingTarget Warehouse warehouse);
}