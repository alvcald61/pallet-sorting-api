package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.order.application.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.PriceDto;
import com.tupack.palletsortingapi.order.application.mapper.PriceConditionDtoMapper;
import com.tupack.palletsortingapi.order.application.mapper.PriceDtoMapper;
import com.tupack.palletsortingapi.order.domain.Price;
import com.tupack.palletsortingapi.order.domain.PriceCondition;
import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.ZoneRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.PriceConditionRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PriceService {

  private final PriceConditionRepository priceConditionRepository;
  private final PriceConditionDtoMapper priceConditionDtoMapper;
  private final ZoneRepository zoneRepository;
  private final PriceDtoMapper priceDtoMapper;
  private final PriceRepository priceRepository;

  public GenericResponse createPrice(PriceDto priceDto) {
    PriceCondition priceCondition = priceConditionDtoMapper.toEntity(priceDto.getPriceCondition());
    priceCondition = priceConditionRepository.save(priceCondition);
    Zone zone = zoneRepository.findById(priceDto.getZone().getId()).orElseThrow();
    Price price = priceDtoMapper.toEntity(priceDto);
    price.setPriceCondition(priceCondition);
    price.setZone(zone);
    price = priceRepository.save(price);
    priceDto = priceDtoMapper.toDto(price);
    return GenericResponse.success(priceDto);
  }
}
