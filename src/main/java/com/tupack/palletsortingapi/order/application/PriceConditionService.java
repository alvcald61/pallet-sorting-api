package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.PriceConditionDto;
import com.tupack.palletsortingapi.order.application.mapper.PriceConditionDtoMapper;
import com.tupack.palletsortingapi.order.domain.PriceCondition;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceConditionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceConditionService {

  private final PriceConditionRepository priceConditionRepository;
  private final PriceConditionDtoMapper priceConditionDtoMapper;

  public GenericResponse getAllPriceConditions() {
    List<PriceConditionDto> conditions = priceConditionRepository.findAllByEnabled(true)
        .stream()
        .map(priceConditionDtoMapper::toDto)
        .toList();
    return GenericResponse.success(conditions);
  }

  public GenericResponse getPriceConditionById(Long id) {
    PriceCondition condition = priceConditionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(
            "Condición de precio no encontrada con id: " + id));
    return GenericResponse.success(priceConditionDtoMapper.toDto(condition));
  }

  @Transactional
  public GenericResponse createPriceCondition(PriceConditionDto dto) {
    PriceCondition condition = priceConditionDtoMapper.toEntity(dto);
    condition.setCreatedAt(LocalDateTime.now());
    condition.setUpdatedAt(LocalDateTime.now());
    condition.setEnabled(true);
    condition = priceConditionRepository.save(condition);
    return GenericResponse.success(priceConditionDtoMapper.toDto(condition));
  }

  @Transactional
  public GenericResponse updatePriceCondition(Long id, PriceConditionDto dto) {
    PriceCondition condition = priceConditionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(
            "Condición de precio no encontrada con id: " + id));

    condition.setCurrency(dto.getCurrency());
    condition.setMinWeight(dto.getMinWeight());
    condition.setMaxWeight(dto.getMaxWeight());
    condition.setMinVolume(dto.getMinVolume());
    condition.setMaxVolume(dto.getMaxVolume());
    condition.setUpdatedAt(LocalDateTime.now());

    condition = priceConditionRepository.save(condition);
    return GenericResponse.success(priceConditionDtoMapper.toDto(condition));
  }

  @Transactional
  public GenericResponse deletePriceCondition(Long id) {
    PriceCondition condition = priceConditionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(
            "Condición de precio no encontrada con id: " + id));
    condition.setEnabled(false);
    condition.setUpdatedAt(LocalDateTime.now());
    priceConditionRepository.save(condition);
    return GenericResponse.success(null, "Condición de precio deshabilitada correctamente");
  }
}
