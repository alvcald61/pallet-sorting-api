package com.tupack.palletsortingapi.order.application.mapper;

import com.tupack.palletsortingapi.order.application.dto.TransportStatusUpdateDto;
import com.tupack.palletsortingapi.order.domain.TransportStatusUpdate;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = ComponentModel.SPRING)
public interface TransportStatusUpdateMapper {
    TransportStatusUpdateDto toDto(TransportStatusUpdate transportStatusUpdate);
}