package com.tupack.palletsortingapi.service;

import com.tupack.palletsortingapi.model.Truck;
import com.tupack.palletsortingapi.repository.TruckRepository;
import com.tupack.palletsortingapi.service.dto.SolutionDto;
import com.tupack.palletsortingapi.service.dto.SolvePackingRequest;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("BULK")
@RequiredArgsConstructor
public class BulkPackingSolution implements Strategy {
  private final TruckRepository truckRepository;

  @Override
  public SolutionDto execute(SolvePackingRequest request) {
    Double totalVolume = getTotalVolume(request);
    Truck truck = truckRepository.findOneByVolume(totalVolume).orElseThrow();
    return SolutionDto.builder().truckId(truck.getId()).build();

  }

  private Double getTotalVolume(SolvePackingRequest request) {
    return request.getPallets().stream()
            .mapToDouble(pallet -> pallet.getVolume() * pallet.getQuantity()).sum();
  }
}
