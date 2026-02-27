package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.DispatcherService;
import com.tupack.palletsortingapi.order.application.dto.DispatcherDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dispatcher")
@RequiredArgsConstructor
class DispatcherController {

  private final DispatcherService dispatcherService;

  @GetMapping
  GenericResponse getDispatchersByClient(@RequestParam Long clientId) {
    return dispatcherService.getDispatchersByClient(clientId);
  }

  @PostMapping
  GenericResponse createDispatcher(@RequestBody DispatcherDto dto) {
    return dispatcherService.createDispatcher(dto);
  }

}
