package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.Truck;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TruckRepository extends JpaRepository<Truck, Long> {

	@Query(
			"SELECT t FROM Truck t WHERE t.volume >= :volumeIsGreaterThan and t.weight >= :totalWeight "
          + "and t.enabled AND t.status = 'AVAILABLE' and t.height >= :maxHeight "
          + "ORDER BY"
          + " t.volume ASC limit"
					+ " 1")
	Optional<Truck> findOneByVolume(Double volumeIsGreaterThan, Double totalWeight, Double maxHeight);

	@Query("SELECT t FROM Truck t WHERE t.weight >= :totalWeight AND t.area >= :area AND t.enabled "
			+ "AND t.status = 'AVAILABLE' AND t.height >= :maxHeight "
			+ "ORDER BY t.weight ASC, t.area ASC")
	List<Truck> findByWeightAndAreaAndHeight(Double totalWeight, Double area, Double maxHeight);

	@Query("SELECT t FROM Truck t WHERE t.width between :width - 0.5  AND :width +  0.5 "
			+ "AND t.length between :length - 0.5 AND :length + 0.5 AND  t.enabled "
			+ "ORDER BY t.weight ASC, t.area ASC")

	Optional<Truck> findSimularDimensionsTruck(Double width, Double length);

  List<Truck> findAllByEnabled(boolean enabled);
}
