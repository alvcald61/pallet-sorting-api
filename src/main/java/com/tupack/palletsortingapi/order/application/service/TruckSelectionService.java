package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.exception.NoTruckAvailableException;
import com.tupack.palletsortingapi.order.application.dto.SolutionDto;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.domain.emuns.TruckStatus;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.TruckRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service responsible for truck selection and availability validation.
 */
@Service
@RequiredArgsConstructor
public class TruckSelectionService {

  private final TruckRepository truckRepository;
  private final OrderRepository orderRepository;

  /**
   * Select an appropriate truck for the order based on the packing solution
   *
   * @param solution Packing solution with recommended truck
   * @param order    Order to be fulfilled
   * @return Selected truck
   * @throws NoTruckAvailableException if no suitable truck is available
   */
  public Truck selectTruck(SolutionDto solution, Order order) {
    Truck truck = solution.getTruck();

    // Check if the recommended truck is available
    if (!isTruckAvailable(truck)) {
      // Find alternative truck with similar dimensions
      truck = findAlternativeTruck(solution, order);
    }

    return truck;
  }

  /**
   * Validate that the truck is available for the requested time slot
   *
   * @param truck                 Selected truck
   * @param pickupDate            Pickup date
   * @param projectedDeliveryDate Projected delivery date
   * @throws com.tupack.palletsortingapi.common.exception.BusinessException if truck is not
   *                                                                         available
   */
  public void validateTruckAvailability(Truck truck, LocalDateTime pickupDate,
      LocalDateTime projectedDeliveryDate) {
    boolean hasConflict = orderRepository.existsOverlappingOrder(
        pickupDate,
        projectedDeliveryDate,
        truck
    );

    if (hasConflict) {
      throw new NoTruckAvailableException(
          String.format("Truck %s is not available for the requested time slot: %s to %s",
              truck.getLicensePlate(), pickupDate, projectedDeliveryDate));
    }
  }

  /**
   * Assign truck to order and update truck status
   *
   * @param truck Truck to assign
   * @param order Order to assign to
   */
  public void assignTruckToOrder(Truck truck, Order order) {
    truck.getOrders().add(order);
    truck.setStatus(TruckStatus.ASSIGNED);
    truckRepository.save(truck);
  }

  /**
   * Check if truck is available (not currently assigned)
   */
  private boolean isTruckAvailable(Truck truck) {
    return truck != null && truck.getStatus() == TruckStatus.AVAILABLE;
  }

  /**
   * Find alternative truck with similar dimensions
   */
  private Truck findAlternativeTruck(SolutionDto solution, Order order) {
    Truck recommendedTruck = solution.getTruck();

    return truckRepository.findSimularDimensionsTruck(
            recommendedTruck.getWidth(),
            recommendedTruck.getLength()
        )
        .orElseThrow(() -> new NoTruckAvailableException(
            String.format("No alternative truck available for pickup date: %s. "
                    + "Required dimensions: %.2f x %.2f",
                order.getPickupDate(),
                recommendedTruck.getWidth(),
                recommendedTruck.getLength())));
  }

  /**
   * Release truck from order (useful for cancellations)
   *
   * @param truck Truck to release
   */
  public void releaseTruck(Truck truck) {
    if (truck != null) {
      truck.setStatus(TruckStatus.AVAILABLE);
      truckRepository.save(truck);
    }
  }
}
