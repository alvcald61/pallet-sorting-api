package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.TransportStatusUpdate;
import com.tupack.palletsortingapi.order.domain.emuns.TransportStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportStatusUpdateRepository extends
    JpaRepository<TransportStatusUpdate, Long> {

  /**
   * Get all transport status updates for an order, ordered by timestamp descending
   */
  List<TransportStatusUpdate> findByOrder_IdOrderByTimestampDesc(Long orderId);

  /**
   * Get all transport status updates for an order, ordered by timestamp ascending
   */
  List<TransportStatusUpdate> findByOrder_IdOrderByTimestampAsc(Long orderId);

  /**
   * Get the latest transport status update for an order
   */
  Optional<TransportStatusUpdate> findFirstByOrder_IdOrderByTimestampDesc(Long orderId);

  /**
   * Get transport status updates by specific status
   */
  List<TransportStatusUpdate> findByOrder_IdAndStatus(Long orderId, TransportStatus status);

  /**
   * Get transport status updates within a date range
   */
  @Query("SELECT t FROM TransportStatusUpdate t WHERE t.order.id = :orderId "
      + "AND t.timestamp BETWEEN :startDate AND :endDate "
      + "ORDER BY t.timestamp DESC")
  List<TransportStatusUpdate> findByOrderIdAndDateRange(
      @Param("orderId") Long orderId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * Get transport status updates with location data
   */
  @Query("SELECT t FROM TransportStatusUpdate t WHERE t.order.id = :orderId "
      + "AND t.locationLatitude IS NOT NULL AND t.locationLongitude IS NOT NULL "
      + "ORDER BY t.timestamp DESC")
  List<TransportStatusUpdate> findByOrderIdWithLocation(@Param("orderId") Long orderId);

  /**
   * Get count of status updates for an order
   */
  long countByOrder_Id(Long orderId);

  /**
   * Check if an order has reached a specific transport status
   */
  boolean existsByOrder_IdAndStatus(Long orderId, TransportStatus status);
}
