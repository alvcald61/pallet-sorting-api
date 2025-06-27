package com.tupack.palletsortingapi.service;

import com.tupack.palletsortingapi.service.dto.SolutionDto;
import com.tupack.palletsortingapi.service.dto.SolvePackingRequest;
import org.springframework.stereotype.Service;

@Service("3D")
public class ThreeDimensionPackingSolution implements Strategy {
  @Override
  public SolutionDto execute(SolvePackingRequest request) {
    return null;
  }
}
