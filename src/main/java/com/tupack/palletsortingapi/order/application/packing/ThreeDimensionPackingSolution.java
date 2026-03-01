package com.tupack.palletsortingapi.order.application.packing;

import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import org.springframework.stereotype.Service;

@Service("3D")
public class ThreeDimensionPackingSolution implements Strategy {
  @Override
  public SolutionDto execute(SolvePackingRequest request) {
    return null;
  }
}
