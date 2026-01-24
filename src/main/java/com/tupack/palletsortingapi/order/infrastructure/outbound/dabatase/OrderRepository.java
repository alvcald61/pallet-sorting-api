package com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase;

import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.Truck;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {
  @Query(value = "SELECT count (o) > 0 from Order o "
          + "where (o.pickupDate not between ?1 and ?2) and (o.projectedDeliveryDate not between ?1 and ?2)")
  Boolean existsOrderInDateRange(LocalDateTime startDate, LocalDateTime endDate, Truck truck);

	@Query(value = "SELECT o.pickupDate from Order o "
			+ "where o.pickupDate between ?1 and ?2")
	List<LocalDateTime> findNotAvailableSlots(LocalDateTime startDate,LocalDateTime endDate);

  Page<Order> getAllByClientId(Long clientId, Pageable pageable);

  Optional<Order> getOrderById(Long id);

  @Query("select o from Order o where o.truck.driver.user.id = :id")
  Page<Order> getAllByDriverId(Long id, Pageable pageable);
}