package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.ZoneDto;
import com.tupack.palletsortingapi.order.application.dto.ZoneRequest;
import com.tupack.palletsortingapi.order.domain.Zone;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface ZoneDtoMapper {
  Zone toEntity(ZoneDto zoneDto);

  Zone toEntity(ZoneRequest request);

  ZoneDto toDto(Zone zone);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Zone partialUpdate(ZoneRequest request, @MappingTarget Zone target);
}
