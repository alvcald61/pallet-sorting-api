package com.tupack.palletsortingapi.controller;

import com.tupack.palletsortingapi.service.OrderService;
import com.tupack.palletsortingapi.service.dto.SolutionDto;
import com.tupack.palletsortingapi.service.dto.SolvePackingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping("/solve/{packingType}")
  public SolutionDto solveOrder(@RequestBody SolvePackingRequest request,
          @PathVariable String packingType) {
    // Logic to solve the order goes here
    return orderService.solvePacking(packingType, request);
  }
}
