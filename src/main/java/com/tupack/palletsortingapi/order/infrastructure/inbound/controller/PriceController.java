package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.order.application.PriceService;
import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.PriceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
@Slf4j
class PriceController {

  private final PriceService priceService;

  @GetMapping
  public GenericResponse getAllPrices(@RequestParam(required = false) Long clientId) {
    return priceService.getAllPrices(clientId);
  }

  @GetMapping("/{id}")
  public GenericResponse getPriceById(@PathVariable Long id) {
    return priceService.getPriceById(id);
  }

  @PostMapping
  public GenericResponse createPrice(@RequestBody PriceDto priceDto) {
    return priceService.createPrice(priceDto);
  }

  @PutMapping("/{id}")
  public GenericResponse updatePrice(@PathVariable Long id, @RequestBody PriceDto priceDto) {
    return priceService.updatePrice(id, priceDto);
  }

  @DeleteMapping("/{id}")
  public GenericResponse deletePrice(@PathVariable Long id) {
    return priceService.deletePrice(id);
  }
}
