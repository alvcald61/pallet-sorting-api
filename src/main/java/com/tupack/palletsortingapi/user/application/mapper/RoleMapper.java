package com.tupack.palletsortingapi.user.application.mapper;

import com.tupack.palletsortingapi.user.application.dto.CreateRoleRequest;
import com.tupack.palletsortingapi.user.application.dto.RoleDto;
import com.tupack.palletsortingapi.user.domain.Role;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface RoleMapper {
  RoleDto toDto(Role role);

  Role toEntity(CreateRoleRequest request);

  void updateEntity(CreateRoleRequest request, @MappingTarget Role role);
}

