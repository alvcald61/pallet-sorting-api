package com.tupack.palletsortingapi.order.application.packing;

import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.TruckRepository;
import com.tupack.palletsortingapi.order.application.dto.PalletBulkDto;
import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
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
    Double totalWeight = request.getTotalWeight();
    Double area = getTotalArea(request);
    Double maxHeight =
        request.getPallets().stream().mapToDouble(PalletBulkDto::getHeight).max().orElse(0.0);
    List<Truck> trucks = truckRepository.findByWeightAndAreaAndHeight(totalWeight, area, maxHeight);
    List<MArea> pieces = getPieces(request);
    for (Truck truck : trucks) {
      Dimension truckDimension =
          new Dimension((int) ((truck.getWidth() - RIGHT_PADDING - LEFT_PADDING) * FACTOR),
              (int) ((truck.getLength() - BOTTOM_PADDING - TOP_PADDING) * FACTOR));
      //      Dimension viewPort = getViewPort(new Dimension((int) ((truck.getWidth()) * FACTOR),
      //              (int) ((truck.getLength()) * FACTOR)));
      Bin[] bins = BinPacking.BinPackingStrategy(pieces.toArray(new MArea[0]), truckDimension,
          truckDimension);
      if (bins.length > 1) {
        continue;
      }
      //      bins[0].getPlacedPieces()[0].getBoundingBox2D()
      var resultPng = SolutionUtils.drawbinToFile(bins, truckDimension);
      var resultTxt = SolutionUtils.createOutputFiles(bins);
      return SolutionDto.builder().truckId(truck.getId()).truck(truck)
          .truckDistributionImageUrl(Paths.get(resultPng.getFirst()).toFile().getAbsolutePath())
          .truckDistributionUrl(Paths.get(resultTxt.getFirst()).toFile().getAbsolutePath()).build();
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
        .mapToDouble(pallet -> pallet.getWidth() * pallet.getLength() * pallet.getQuantity()).sum();

  }

  private Double getTotalWeight(SolvePackingRequest request) {
    return request.getPallets().stream()
        .mapToDouble(pallet -> pallet.getWeight() * pallet.getQuantity()).sum();
  }
}
