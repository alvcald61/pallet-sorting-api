package com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase;

import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.Truck;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
public interface OrderRepository extends JpaRepository<Order, String> {
  @Query(value = "SELECT count (o) > 0 from Order o "
          + "where (o.pickupDate not between ?1 and ?2) and (o.projectedDeliveryDate not between ?1 and ?2)")
  Boolean existsOrderInDateRange(LocalDateTime startDate, LocalDateTime endDate, Truck truck);
}