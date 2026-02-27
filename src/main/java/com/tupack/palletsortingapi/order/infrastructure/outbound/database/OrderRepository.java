package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByClientDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByDriverDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByStatusDTO;
import com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByTruckDTO;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
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

  // Dashboard optimized queries
  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus IN :statuses")
  long countByStatusIn(@Param("statuses") List<OrderStatus> statuses);

  @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o")
  BigDecimal sumAllAmounts();

  @Query("SELECT COALESCE(SUM(o.totalVolume), 0) FROM Order o")
  BigDecimal sumTotalVolume();

  @Query("SELECT COALESCE(SUM(o.totalWeight), 0) FROM Order o")
  BigDecimal sumTotalWeight();

  @Query("SELECT new com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByStatusDTO(" +
      "o.orderStatus, COUNT(o)) " +
      "FROM Order o GROUP BY o.orderStatus")
  List<OrdersByStatusDTO> countOrdersByStatus();

  @Query("SELECT new com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByClientDTO(" +
      "CAST(c.id AS string), " +
      "CONCAT(c.user.firstName, ' ', c.user.lastName), " +
      "c.businessName, " +
      "COUNT(o)) " +
      "FROM Order o JOIN o.client c " +
      "GROUP BY c.id, c.user.firstName, c.user.lastName, c.businessName")
  List<OrdersByClientDTO> countOrdersByClient();

  @Query("SELECT new com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByDriverDTO(" +
      "CAST(d.driverId AS string), " +
      "CONCAT(d.user.firstName, ' ', d.user.lastName), " +
      "CONCAT(d.user.firstName, ' ', d.user.lastName), " +
      "COUNT(o)) " +
      "FROM Order o JOIN o.truck t JOIN t.driver d " +
      "WHERE t IS NOT NULL AND d IS NOT NULL " +
      "GROUP BY d.driverId, d.user.firstName, d.user.lastName")
  List<OrdersByDriverDTO> countOrdersByDriver();

  @Query("SELECT new com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByTruckDTO(" +
      "CAST(t.id AS string), " +
      "t.licensePlate, " +
      "t.licensePlate, " +
      "COUNT(o)) " +
      "FROM Order o JOIN o.truck t " +
      "WHERE t IS NOT NULL " +
      "GROUP BY t.id, t.licensePlate")
  List<OrdersByTruckDTO> countOrdersByTruck();

  @EntityGraph(attributePaths = {"client", "client.user"})
  @Query("SELECT o FROM Order o WHERE o.orderStatus IN :statuses ORDER BY o.pickupDate ASC")
  List<Order> findByStatusInOrderByPickupDateAsc(
      @Param("statuses") List<OrderStatus> statuses,
      Pageable pageable);

  // --- Dashboard date-filtered queries ---

  @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus IN :statuses AND o.pickupDate BETWEEN :startDate AND :endDate")
  long countByStatusInAndDateRange(@Param("statuses") List<OrderStatus> statuses,
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.pickupDate BETWEEN :startDate AND :endDate")
  BigDecimal sumAllAmountsInDateRange(@Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT COALESCE(SUM(o.totalVolume), 0) FROM Order o WHERE o.pickupDate BETWEEN :startDate AND :endDate")
  BigDecimal sumTotalVolumeInDateRange(@Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT COALESCE(SUM(o.totalWeight), 0) FROM Order o WHERE o.pickupDate BETWEEN :startDate AND :endDate")
  BigDecimal sumTotalWeightInDateRange(@Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT new com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByStatusDTO(" +
      "o.orderStatus, COUNT(o)) " +
      "FROM Order o WHERE o.pickupDate BETWEEN :startDate AND :endDate GROUP BY o.orderStatus")
  List<OrdersByStatusDTO> countOrdersByStatusInDateRange(@Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT new com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByClientDTO(" +
      "CAST(c.id AS string), " +
      "CONCAT(c.user.firstName, ' ', c.user.lastName), " +
      "c.businessName, COUNT(o)) " +
      "FROM Order o JOIN o.client c " +
      "WHERE o.pickupDate BETWEEN :startDate AND :endDate " +
      "GROUP BY c.id, c.user.firstName, c.user.lastName, c.businessName")
  List<OrdersByClientDTO> countOrdersByClientInDateRange(@Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT new com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByDriverDTO(" +
      "CAST(d.driverId AS string), " +
      "CONCAT(d.user.firstName, ' ', d.user.lastName), " +
      "CONCAT(d.user.firstName, ' ', d.user.lastName), COUNT(o)) " +
      "FROM Order o JOIN o.truck t JOIN t.driver d " +
      "WHERE t IS NOT NULL AND d IS NOT NULL AND o.pickupDate BETWEEN :startDate AND :endDate " +
      "GROUP BY d.driverId, d.user.firstName, d.user.lastName")
  List<OrdersByDriverDTO> countOrdersByDriverInDateRange(@Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query("SELECT new com.tupack.palletsortingapi.order.application.dto.dashboard.OrdersByTruckDTO(" +
      "CAST(t.id AS string), t.licensePlate, t.licensePlate, COUNT(o)) " +
      "FROM Order o JOIN o.truck t " +
      "WHERE t IS NOT NULL AND o.pickupDate BETWEEN :startDate AND :endDate " +
      "GROUP BY t.id, t.licensePlate")
  List<OrdersByTruckDTO> countOrdersByTruckInDateRange(@Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @EntityGraph(attributePaths = {"client", "client.user"})
  @Query("SELECT o FROM Order o WHERE o.orderStatus IN :statuses " +
      "AND o.pickupDate BETWEEN :startDate AND :endDate ORDER BY o.pickupDate ASC")
  List<Order> findByStatusInAndDateRangeOrderByPickupDateAsc(
      @Param("statuses") List<OrderStatus> statuses,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate,
      Pageable pageable);
}
