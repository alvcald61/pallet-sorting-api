package com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase;

import com.tupack.palletsortingapi.order.domain.Zone;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
public interface ZoneRepository extends JpaRepository<Zone, Long> {
  List<Zone> findAllByEnabled(boolean enabled);


  @Query("select z from Zone z where z.district like concat('%', ?1, '%') and z.enabled = true")
  Optional<Zone> findZoneByDistrictContaining(String district);
}