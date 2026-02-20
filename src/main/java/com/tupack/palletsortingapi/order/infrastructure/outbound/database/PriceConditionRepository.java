package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.PriceCondition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PriceConditionRepository extends JpaRepository<PriceCondition, Long> {

  List<PriceCondition> findAllByEnabled(boolean enabled);

  /**
   * Busca una condición de precio que coincida con el peso dado.
   * Si la condición tiene volumen = 0 (minVolume=0 y maxVolume=0), solo aplica restricción de peso.
   * Si la condición tiene volumen > 0, ambas restricciones (peso y volumen) deben coincidir.
   */
  @Query("""
    select pc from PriceCondition pc
    where :weight between pc.minWeight and pc.maxWeight
    and (
      (pc.minVolume = 0 and pc.maxVolume = 0)
      or :volume between pc.minVolume and pc.maxVolume
    )
    and pc.enabled = true
""")
  Optional<PriceCondition> findByVolumeAndWeight(Double volume, Double weight);
}
