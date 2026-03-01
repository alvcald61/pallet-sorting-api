package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.OrderStatusUpdateDto;
import com.tupack.palletsortingapi.order.domain.OrderStatusUpdate;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface OrderStatusUpdateMapper {
  OrderStatusUpdate toEntity(OrderStatusUpdateDto orderStatusUpdateDto);

  OrderStatusUpdateDto toDto(OrderStatusUpdate orderStatusUpdate);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  OrderStatusUpdate partialUpdate(OrderStatusUpdateDto orderStatusUpdateDto,
      @MappingTarget OrderStatusUpdate orderStatusUpdate);
}
