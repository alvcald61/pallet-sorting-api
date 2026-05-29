package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.PriceConditionDto;
import com.tupack.palletsortingapi.order.application.dto.PriceConditionRequest;
import com.tupack.palletsortingapi.order.domain.PriceCondition;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface PriceConditionDtoMapper {
  PriceCondition toEntity(PriceConditionDto priceConditionDto);

  PriceCondition toEntity(PriceConditionRequest request);

  PriceConditionDto toDto(PriceCondition priceCondition);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  PriceCondition partialUpdate(PriceConditionRequest request, @MappingTarget PriceCondition target);
}
