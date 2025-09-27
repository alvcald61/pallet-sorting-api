package com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase;

import com.tupack.palletsortingapi.order.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
public interface ZoneRepository extends JpaRepository<Zone, String> {
}