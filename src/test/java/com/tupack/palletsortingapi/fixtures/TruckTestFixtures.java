package com.tupack.palletsortingapi.fixtures;

import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.domain.enums.TruckStatus;
import java.math.BigDecimal;

/**
 * Test fixtures for Truck entities.
 * Provides factory methods for creating test data.
 */
public class TruckTestFixtures {

    public static Truck createAvailableTruck() {
        Truck truck = new Truck();
        truck.setId(1L);
        truck.setStatus(TruckStatus.AVAILABLE);
        truck.setHeight((2.5));
        truck.setWidth((2.0));
        truck.setLength(6.0);
        truck.setLicensePlate("ABC-123");
        return truck;
    }

    public static Truck createTruckWithId(Long id) {
        Truck truck = createAvailableTruck();
        truck.setId(id);
        return truck;
    }

    public static Truck createInUseTruck() {
        Truck truck = createAvailableTruck();
        truck.setId(2L);
        truck.setStatus(TruckStatus.ASSIGNED);
        truck.setLicensePlate("XYZ-789");
        return truck;
    }
}
