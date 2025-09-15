package com.tupack.palletsortingapi.service;

import com.tupack.palletsortingapi.service.dto.SolutionDto;
import com.tupack.palletsortingapi.service.dto.SolvePackingRequest;
import java.io.IOException;

public interface Strategy {
    /**
     * Executes the sorting strategy.
     *
     * @return a String indicating the result of the sorting operation
     */
    SolutionDto execute(SolvePackingRequest request) throws IOException;
}
