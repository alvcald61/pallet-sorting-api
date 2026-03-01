package com.tupack.palletsortingapi.order.application.packing;

import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import java.io.IOException;

public interface Strategy {
    /**
     * Executes the sorting strategy.
     *
     * @return a String indicating the result of the sorting operation
     */
    SolutionDto execute(SolvePackingRequest request) throws IOException;
}
