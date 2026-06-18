package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.PriceConditionDto;
import com.tupack.palletsortingapi.order.application.dto.PriceConditionRequest;
import com.tupack.palletsortingapi.order.application.mapper.PriceConditionDtoMapper;
import com.tupack.palletsortingapi.order.domain.PriceCondition;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceConditionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceConditionService {

  private final PriceConditionRepository priceConditionRepository;
  private final PriceConditionDtoMapper priceConditionDtoMapper;

  @Cacheable("price-conditions")
  public GenericResponse getAllPriceConditions() {
    List<PriceConditionDto> conditions = priceConditionRepository.findAllByEnabled(true)
        .stream()
        .map(priceConditionDtoMapper::toDto)
        .toList();
    return GenericResponse.success(conditions);
  }

  @Cacheable(value = "price-condition", key = "#id")
  public GenericResponse getPriceConditionById(Long id) {
    PriceCondition condition = priceConditionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(
            "Condición de precio no encontrada con id: " + id));
    return GenericResponse.success(priceConditionDtoMapper.toDto(condition));
  }

  @Transactional
  @CacheEvict(value = "price-conditions", allEntries = true)
  public GenericResponse createPriceCondition(PriceConditionRequest request) {
    PriceCondition condition = priceConditionDtoMapper.toEntity(request);
    condition.setEnabled(true);
    condition = priceConditionRepository.save(condition);
    return GenericResponse.success(priceConditionDtoMapper.toDto(condition));
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "price-conditions", allEntries = true),
      @CacheEvict(value = "price-condition", key = "#id"),
  })
  public GenericResponse updatePriceCondition(Long id, PriceConditionRequest request) {
    PriceCondition condition = priceConditionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(
            "Condición de precio no encontrada con id: " + id));

    priceConditionDtoMapper.partialUpdate(request, condition);
    condition = priceConditionRepository.save(condition);
    return GenericResponse.success(priceConditionDtoMapper.toDto(condition));
  }

  @Transactional
  @Caching(evict = {
      @CacheEvict(value = "price-conditions", allEntries = true),
      @CacheEvict(value = "price-condition", key = "#id"),
  })
  public GenericResponse deletePriceCondition(Long id) {
    PriceCondition condition = priceConditionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(
            "Condición de precio no encontrada con id: " + id));
    condition.setEnabled(false);
    priceConditionRepository.save(condition);
    return GenericResponse.success(null, "Condición de precio deshabilitada correctamente");
  }
}
