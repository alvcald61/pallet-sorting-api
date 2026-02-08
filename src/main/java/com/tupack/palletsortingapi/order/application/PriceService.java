package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.ZoneNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.PriceDto;
import com.tupack.palletsortingapi.order.application.mapper.PriceConditionDtoMapper;
import com.tupack.palletsortingapi.order.application.mapper.PriceDtoMapper;
import com.tupack.palletsortingapi.order.domain.Price;
import com.tupack.palletsortingapi.order.domain.PriceCondition;
import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.ZoneRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceConditionRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceService {

  private final PriceConditionRepository priceConditionRepository;
  private final PriceConditionDtoMapper priceConditionDtoMapper;
  private final ZoneRepository zoneRepository;
  private final PriceDtoMapper priceDtoMapper;
  private final PriceRepository priceRepository;

  public GenericResponse createPrice(final PriceDto priceDto) {
    PriceCondition priceCondition = priceConditionDtoMapper.toEntity(priceDto.getPriceCondition());
    priceCondition = priceConditionRepository.save(priceCondition);
    Zone zone = zoneRepository.findById(priceDto.getZone().getId())
        .orElseThrow(() -> new ZoneNotFoundException(priceDto.getZone().getId()));
    Price price = priceDtoMapper.toEntity(priceDto);
    price.setPriceCondition(priceCondition);
    price.setZone(zone);
    price = priceRepository.save(price);
    return GenericResponse.success( priceDtoMapper.toDto(price));
  }
}
