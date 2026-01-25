package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.PriceDto;
import com.tupack.palletsortingapi.order.domain.Price;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface PriceDtoMapper {
  Price toEntity(PriceDto priceDto);
  PriceDto toDto(Price price);
}
