package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.ZoneDto;
import com.tupack.palletsortingapi.order.domain.Zone;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface ZoneDtoMapper {
  Zone toEntity(ZoneDto zoneDto);
  ZoneDto toDto(Zone zone);
}
