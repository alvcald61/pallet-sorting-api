package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.Truck;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT t FROM Truck t WHERE t.id = :id")
  Optional<Truck> findByIdWithLock(@Param("id") Long id);

  @Query("SELECT t FROM Truck t WHERE t.width BETWEEN :width - 0.5 AND :width + 0.5 "
      + "AND t.length BETWEEN :length - 0.5 AND :length + 0.5 AND t.enabled "
      + "AND t.status = 'AVAILABLE' "
      + "AND NOT EXISTS (SELECT o FROM Order o WHERE o.truck = t "
      + "AND o.pickupDate <= :endDate AND o.projectedDeliveryDate >= :startDate) "
      + "ORDER BY t.weight ASC, t.area ASC")
  Optional<Truck> findSimilarAvailableTruck(@Param("width") Double width,
      @Param("length") Double length,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  List<Truck> findAllByEnabled(boolean enabled);
}
