package com.tupack.palletsortingapi.order.application.packing;

import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PackingStrategyExecutor {
  private final Map<String, Strategy> strategies;

  public SolutionDto execute(String strategyType, SolvePackingRequest request) {
    Strategy strategy = strategies.get(strategyType);
    if (strategy == null) {
      throw new IllegalArgumentException("No packing strategy found for type: " + strategyType);
    }
    try {
      return strategy.execute(request);
    } catch (IOException e) {
      throw new RuntimeException("Error executing packing strategy: " + strategyType, e);
    }
  }
}
