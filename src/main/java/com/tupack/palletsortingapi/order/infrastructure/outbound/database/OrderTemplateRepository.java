package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.OrderTemplate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderTemplateRepository extends JpaRepository<OrderTemplate, Long> {

  @Query("SELECT t FROM OrderTemplate t WHERE t.client.id = :clientId ORDER BY t.lastUsed DESC, t.createdAt DESC")
  List<OrderTemplate> findByClientIdOrderByLastUsedDesc(@Param("clientId") Long clientId);
}
