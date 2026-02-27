package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.Price;
import com.tupack.palletsortingapi.order.domain.PriceCondition;
import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.user.domain.Client;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

  List<Price> findAllByEnabled(boolean enabled);

  List<Price> findAllByEnabledAndClientId(boolean enabled, Long clientId);

  @Query("select p from Price p where p.zone = :zone and p.priceCondition = :priceCondition and p.client = :client and p.enabled = true")
  Price findByZoneAndPriceConditionAndClient(
      @Param("zone") Zone zone, @Param("priceCondition") PriceCondition priceCondition,
      @Param("client") Client client);
}
