package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.PriceConditionDto;
import com.tupack.palletsortingapi.order.domain.PriceCondition;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface PriceConditionDtoMapper {
  PriceCondition toEntity(PriceConditionDto priceConditionDto);
}