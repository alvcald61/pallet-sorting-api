package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.application.packing.PackingStrategyExecutor;
import com.tupack.palletsortingapi.utils.PackingType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPackingService {

  private final PackingStrategyExecutor context;

  public SolutionDto solvePacking(String packingType, SolvePackingRequest request) {
    if (packingType == null || packingType.isEmpty()) {
      throw new IllegalArgumentException("Packing type must not be null or empty");
    }
    return switch (PackingType.valueOf(packingType)) {
      case PackingType.BULK -> context.execute(PackingType.BULK.getName(), request);
      case PackingType.TWO_DIMENSIONAL ->
          context.execute(PackingType.TWO_DIMENSIONAL.getName(), request);
      case PackingType.THREE_DIMENSIONAL ->
          context.execute(PackingType.THREE_DIMENSIONAL.getName(), request);
    };
  }
}
