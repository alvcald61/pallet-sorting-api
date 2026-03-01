package com.tupack.palletsortingapi.user.application.mapper;

import com.tupack.palletsortingapi.user.application.dto.CreateDriverRequest;
import com.tupack.palletsortingapi.user.application.dto.DriverDto;
import com.tupack.palletsortingapi.user.domain.Driver;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface DriverMapper {

  @Mapping(target = "email", source = "user.email")
  @Mapping(target = "firstName", source = "user.firstName")
  @Mapping(target = "lastName", source = "user.lastName")
  @Mapping(target = "driverLicence", source = "driverLicence")
  DriverDto toDto(Driver driver);

  Driver toEntity(CreateDriverRequest request);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntity(CreateDriverRequest request, @MappingTarget Driver driver);
}

