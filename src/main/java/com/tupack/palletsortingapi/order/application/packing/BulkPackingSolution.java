package com.tupack.palletsortingapi.order.application.packing;

import com.tupack.palletsortingapi.order.application.dto.PalletBulkDto;
import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.TruckRepository;
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
    Double totalWeight = request.getPallets().stream()
        .mapToDouble(pallet -> pallet.getWeight() * pallet.getQuantity()).sum();
    Double maxHeight = request.getPallets().stream()
        .mapToDouble(PalletBulkDto::getHeight).max().orElse(0.0);
    Truck truck = truckRepository.findOneByVolume(totalVolume, totalWeight, maxHeight).orElseThrow();
    return SolutionDto.builder().truckId(truck.getId()).truck(truck).build();

  }

  private Double getTotalVolume(SolvePackingRequest request) {
    return request.getPallets().stream()
        .mapToDouble(pallet -> pallet.getVolume() * pallet.getQuantity()).sum();
  }
}
