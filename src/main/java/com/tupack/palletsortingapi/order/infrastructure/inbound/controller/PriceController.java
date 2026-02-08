package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.order.application.PriceService;
import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.PriceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
@Slf4j
class PriceController {

  private final PriceService priceService;

  @PostMapping
  public GenericResponse createPrice(@RequestBody PriceDto priceDto) {
    return priceService.createPrice(priceDto);
  }
}
