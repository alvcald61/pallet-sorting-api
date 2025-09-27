package com.tupack.palletsortingapi.order.application.packing;

import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.TruckRepository;
import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
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
