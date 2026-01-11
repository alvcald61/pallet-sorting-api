package com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase;

import com.tupack.palletsortingapi.order.domain.PriceCondition;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PriceConditionRepository extends JpaRepository<PriceCondition, Long> {
  @Query("""
    select pc from PriceCondition pc
    where :volume between pc.minVolume and pc.maxVolume
    and :weight between pc.minWeight and pc.maxWeight
    and pc.enabled = true
""")
  Optional<PriceCondition> findByVolumeAndWeight(Double volume, Double weight);
}
