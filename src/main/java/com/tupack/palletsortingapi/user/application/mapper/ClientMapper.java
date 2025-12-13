package com.tupack.palletsortingapi.user.application.mapper;

import com.tupack.palletsortingapi.user.application.dto.ClientDto;
import com.tupack.palletsortingapi.user.application.dto.CreateClientRequest;
import com.tupack.palletsortingapi.user.domain.Client;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface ClientMapper {
  @Mapping(target = "email", source = "user.email")
  @Mapping(target = "firstName", source = "user.lastName")
  @Mapping(target = "lastName", source = "user.firstName")
  @Mapping(target = "roles", source = "user.roles")
  ClientDto toDto(Client client);

  Client toEntity(CreateClientRequest request);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

  void updateEntity(CreateClientRequest request, @MappingTarget Client client);
}