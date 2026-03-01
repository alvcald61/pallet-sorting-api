package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.PriceConditionDto;
import com.tupack.palletsortingapi.order.application.service.PriceConditionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/price-condition")
@RequiredArgsConstructor
@Slf4j
class PriceConditionController {

  private final PriceConditionService priceConditionService;

  @GetMapping
  public GenericResponse getAllPriceConditions() {
    return priceConditionService.getAllPriceConditions();
  }

  @GetMapping("/{id}")
  public GenericResponse getPriceConditionById(@PathVariable Long id) {
    return priceConditionService.getPriceConditionById(id);
  }

  @PostMapping
  public GenericResponse createPriceCondition(@RequestBody PriceConditionDto dto) {
    return priceConditionService.createPriceCondition(dto);
  }

  @PutMapping("/{id}")
  public GenericResponse updatePriceCondition(
      @PathVariable Long id, @RequestBody PriceConditionDto dto) {
    return priceConditionService.updatePriceCondition(id, dto);
  }

  @DeleteMapping("/{id}")
  public GenericResponse deletePriceCondition(@PathVariable Long id) {
    return priceConditionService.deletePriceCondition(id);
  }
}
