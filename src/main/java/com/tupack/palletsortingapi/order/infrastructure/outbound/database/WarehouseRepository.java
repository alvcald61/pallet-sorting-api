package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.Warehouse;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

  Page<Warehouse> findAllByEnabled(boolean enabled, Pageable pageable);

  Optional<Warehouse> findByWarehouseIdAndEnabled(Long warehouseId, boolean enabled);
}
