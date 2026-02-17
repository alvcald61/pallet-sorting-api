package com.tupack.palletsortingapi.user.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.user.domain.Driver;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DriverRepository extends JpaRepository<Driver, Long> {
  Optional<Driver> findDriverByUserId(Long userId);

  List<Driver> findAllByEnabled(boolean enabled);

  Driver getDriverByDriverId(Long id);

  Optional<Driver> findByDni(String dni);

  @Query("select d from Driver d where d.truck is null")
  List<Driver> findAllByTruckNull();
}

