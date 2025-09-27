package com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase;

import com.tupack.palletsortingapi.order.domain.TruckOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TruckOrderRepository extends JpaRepository<TruckOrder, String> {
}