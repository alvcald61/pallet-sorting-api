package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.PriceDto;
import com.tupack.palletsortingapi.order.domain.Price;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface PriceDtoMapper {

  @Mapping(target = "client", ignore = true)
  Price toEntity(PriceDto priceDto);

  @Mapping(source = "client.id", target = "clientId")
  @Mapping(source = "client.businessName", target = "clientBusinessName")
  PriceDto toDto(Price price);
}
