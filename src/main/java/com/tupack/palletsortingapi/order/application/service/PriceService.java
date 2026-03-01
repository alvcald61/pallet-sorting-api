package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.ZoneNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.PriceDto;
import com.tupack.palletsortingapi.order.application.mapper.PriceConditionDtoMapper;
import com.tupack.palletsortingapi.order.application.mapper.PriceDtoMapper;
import com.tupack.palletsortingapi.order.domain.Price;
import com.tupack.palletsortingapi.order.domain.PriceCondition;
import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.ZoneRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceConditionRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceService {

  private final PriceConditionRepository priceConditionRepository;
  private final PriceConditionDtoMapper priceConditionDtoMapper;
  private final ZoneRepository zoneRepository;
  private final PriceDtoMapper priceDtoMapper;
  private final PriceRepository priceRepository;
  private final ClientRepository clientRepository;

  public GenericResponse getAllPrices(Long clientId) {
    List<Price> priceList;
    if (clientId != null) {
      priceList = priceRepository.findAllByEnabledAndClientId(true, clientId);
    } else {
      priceList = priceRepository.findAllByEnabled(true);
    }
    List<PriceDto> prices = priceList.stream()
        .map(priceDtoMapper::toDto)
        .toList();
    return GenericResponse.success(prices);
  }

  public GenericResponse getPriceById(Long id) {
    Price price = priceRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Precio no encontrado con id: " + id));
    return GenericResponse.success(priceDtoMapper.toDto(price));
  }

  @Transactional
  public GenericResponse createPrice(final PriceDto priceDto) {
    PriceCondition priceCondition;
    // Si viene un ID de condición existente, reutilizarla
    if (priceDto.getPriceCondition() != null
        && priceDto.getPriceCondition().getPriceConditionId() != null) {
      priceCondition = priceConditionRepository
          .findById(priceDto.getPriceCondition().getPriceConditionId())
          .orElseThrow(() -> new EntityNotFoundException(
              "Condición de precio no encontrada con id: "
                  + priceDto.getPriceCondition().getPriceConditionId()));
    } else {
      // Crear nueva condición si no viene ID
      priceCondition = priceConditionDtoMapper.toEntity(priceDto.getPriceCondition());
      priceCondition = priceConditionRepository.save(priceCondition);
    }

    Zone zone = zoneRepository.findById(priceDto.getZone().getId())
        .orElseThrow(() -> new ZoneNotFoundException(priceDto.getZone().getId()));

    Price price = priceDtoMapper.toEntity(priceDto);
    price.setPriceCondition(priceCondition);
    price.setZone(zone);

    if (priceDto.getClientId() != null) {
      Client client = clientRepository.findById(priceDto.getClientId())
          .orElseThrow(() -> new EntityNotFoundException(
              "Cliente no encontrado con id: " + priceDto.getClientId()));
      price.setClient(client);
    }

    price = priceRepository.save(price);
    return GenericResponse.success(priceDtoMapper.toDto(price));
  }

  @Transactional
  public GenericResponse updatePrice(Long id, PriceDto priceDto) {
    Price price = priceRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Precio no encontrado con id: " + id));

    price.setPrice(priceDto.getPrice());

    if (priceDto.getZone() != null && priceDto.getZone().getId() != null) {
      Zone zone = zoneRepository.findById(priceDto.getZone().getId())
          .orElseThrow(() -> new ZoneNotFoundException(priceDto.getZone().getId()));
      price.setZone(zone);
    }

    if (priceDto.getPriceCondition() != null
        && priceDto.getPriceCondition().getPriceConditionId() != null) {
      PriceCondition condition = priceConditionRepository
          .findById(priceDto.getPriceCondition().getPriceConditionId())
          .orElseThrow(() -> new EntityNotFoundException(
              "Condición de precio no encontrada con id: "
                  + priceDto.getPriceCondition().getPriceConditionId()));
      price.setPriceCondition(condition);
    }

    if (priceDto.getClientId() != null) {
      Client client = clientRepository.findById(priceDto.getClientId())
          .orElseThrow(() -> new EntityNotFoundException(
              "Cliente no encontrado con id: " + priceDto.getClientId()));
      price.setClient(client);
    }

    price = priceRepository.save(price);
    return GenericResponse.success(priceDtoMapper.toDto(price));
  }

  @Transactional
  public GenericResponse deletePrice(Long id) {
    Price price = priceRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Precio no encontrado con id: " + id));
    price.setEnabled(false);
    priceRepository.save(price);
    return GenericResponse.success(null, "Precio deshabilitado correctamente");
  }
}
