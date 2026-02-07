package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.Truck;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
  @Query("select count(o) > 0 from Order o where o.truck = :truck "
      + "and o.pickupDate <= :endDate and o.projectedDeliveryDate >= :startDate")
  Boolean existsOverlappingOrder(@Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate, @Param("truck") Truck truck);

	@Query(value = "SELECT o.pickupDate from Order o "
			+ "where o.pickupDate between ?1 and ?2")
	List<LocalDateTime> findNotAvailableSlots(LocalDateTime startDate,LocalDateTime endDate);

  @EntityGraph(attributePaths = {"truck", "truck.driver", "client", "warehouse"})
  Page<Order> getAllByClientId(Long clientId, Pageable pageable);

  @EntityGraph(attributePaths = {"truck", "truck.driver", "client", "warehouse", "zone"})
  Optional<Order> getOrderById(Long id);

  @EntityGraph(attributePaths = {"truck", "truck.driver", "client", "warehouse"})
  @Query("select o from Order o where o.truck.driver.user.id = :id")
  Page<Order> getAllByDriverId(Long id, Pageable pageable);
}
