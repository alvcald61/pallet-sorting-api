package com.tupack.palletsortingapi.service;

import com.tupack.palletsortingapi.model.Truck;
import com.tupack.palletsortingapi.repository.TruckRepository;
import com.tupack.palletsortingapi.service.dto.PalletBulkDto;
import com.tupack.palletsortingapi.service.dto.SolutionDto;
import com.tupack.palletsortingapi.service.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.utils.SolutionUtils;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.packing.core.Bin;
import org.packing.core.BinPacking;
import org.packing.primitives.MArea;
import org.springframework.stereotype.Service;

@Service("2D")
@RequiredArgsConstructor
public class TwoDimensionPackingSolution implements Strategy {

  public static final int FACTOR = 1000;
  public static final Double RIGHT_PADDING = 0.1;
  public static final Double LEFT_PADDING = 0.0;
  public static final Double BOTTOM_PADDING = 0.0;
  public static final Double TOP_PADDING = 0.0;

  private final TruckRepository truckRepository;

  @Override
  public SolutionDto execute(SolvePackingRequest request) throws IOException {
    Double totalWeight = getTotalWeight(request);
    Double area = getTotalArea(request);
    List<Truck> trucks = truckRepository.findByWeightAndArea(totalWeight, area);
    List<MArea> pieces = getPieces(request);
    for (Truck truck : trucks) {
      Dimension truckDimension = new Dimension(
              (int) ((truck.getWidth() - RIGHT_PADDING - LEFT_PADDING) * FACTOR),
              (int) ((truck.getLength() - BOTTOM_PADDING - TOP_PADDING) * FACTOR));
//      Dimension viewPort = getViewPort(new Dimension((int) ((truck.getWidth()) * FACTOR),
//              (int) ((truck.getLength()) * FACTOR)));
      Bin[] bins = BinPacking.BinPackingStrategy(pieces.toArray(new MArea[0]), truckDimension,
              truckDimension);
      //      if (bins.length > 1) {
      //        continue;
      //      }
      //      bins[0].getPlacedPieces()[0].getBoundingBox2D()
      SolutionUtils.drawbinToFile(bins, truckDimension);
      SolutionUtils.createOutputFiles(bins);
      return SolutionDto.builder().truckId(truck.getId())
              .truckDistributionImageUrl(Paths.get("Bin-1.png").toFile().getAbsolutePath())
              .truckDistributionUrl(Paths.get("Bin-1.txt").toFile().getAbsolutePath()).build();
    }
    throw new IllegalArgumentException("No suitable truck found for the given request");
  }

  private List<MArea> getPieces(SolvePackingRequest request) {
    if (request.getPallets() == null || request.getPallets().isEmpty()) {
      throw new IllegalArgumentException("Pieces must not be null or empty");
    }
    List<MArea> pieces = new ArrayList<>(request.getPallets().size());
    int i = 0;
    for (PalletBulkDto palletBulkDto : request.getPallets()) {
      if (palletBulkDto.getWidth() <= 0 || palletBulkDto.getLength() <= 0) {
        throw new IllegalArgumentException("Piece dimensions must be greater than zero");
      }
      for (int j = 0; j < palletBulkDto.getQuantity(); j++) {
        pieces.add(new MArea(new Rectangle(0, 0, (int) (palletBulkDto.getWidth() * FACTOR),
                (int) (palletBulkDto.getLength() * FACTOR)), ++i));
      }

    }
    return pieces;
  }

  private Dimension getViewPort(Dimension truck) {
    Dimension viewPortDimension;
    Double x1 = truck.getWidth();
    Double y1 = truck.getHeight();
    if (x1 > y1) {
      viewPortDimension = new Dimension(1500, (int) (1500 / (x1 / y1)));
    } else {
      viewPortDimension = new Dimension((int) (1500 / (y1 / x1)), 1500);
    }
    return viewPortDimension;
  }

  private Double getTotalArea(SolvePackingRequest request) {
    return request.getPallets().stream()
            .mapToDouble(pallet -> pallet.getWidth() * pallet.getLength() * pallet.getQuantity())
            .sum();

  }

  private Double getTotalWeight(SolvePackingRequest request) {
    return request.getPallets().stream()
            .mapToDouble(pallet -> pallet.getWeight() * pallet.getQuantity()).sum();
  }
}
