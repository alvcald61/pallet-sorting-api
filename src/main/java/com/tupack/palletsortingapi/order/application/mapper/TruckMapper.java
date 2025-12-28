package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.TruckDto;
import com.tupack.palletsortingapi.order.domain.Truck;
import java.awt.print.Book;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface TruckMapper {
  Truck toEntity(TruckDto truckDto);

  @Mapping(target = "driverId", source = "driver.driverId")
  @Mapping(target = "driverName", source = ".", qualifiedByName = "getFullName")
  TruckDto toDto(Truck truck);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntity(TruckDto truckDto, @MappingTarget Truck truck);

  @Named("getFullName")
  default String getFullName(Truck truck) {
    if(truck.getDriver() == null || truck.getDriver().getUser() == null) {
      return null;
    }
    return truck.getDriver().getUser().getFirstName() + " " + truck.getDriver().getUser().getLastName();

  }
}
