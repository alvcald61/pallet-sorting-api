package com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase;

import com.tupack.palletsortingapi.order.domain.Truck;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TruckRepository extends JpaRepository<Truck, String> {

  @Query("SELECT t FROM Truck t WHERE t.volume > :volumeIsGreaterThan and t.enabled ORDER BY t.volume ASC limit"
          + " 1")
  Optional<Truck> findOneByVolume(Double volumeIsGreaterThan);

  @Query("SELECT t FROM Truck t WHERE t.weight >= :totalWeight AND t.area >= :area AND t.enabled "
          + "ORDER BY t.weight ASC, t.area ASC")
  List<Truck> findByWeightAndArea(Double totalWeight, Double area);
}
