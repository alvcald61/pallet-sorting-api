package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.DispatcherDto;
import com.tupack.palletsortingapi.order.domain.Dispatcher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface DispatcherDtoMapper {

  @Mapping(source = "client.id", target = "clientId")
  DispatcherDto toDto(Dispatcher dispatcher);

  @Mapping(target = "client", ignore = true)
  Dispatcher toEntity(DispatcherDto dto);
}
