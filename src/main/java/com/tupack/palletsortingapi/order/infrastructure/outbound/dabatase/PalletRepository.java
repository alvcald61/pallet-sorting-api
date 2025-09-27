package com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase;

import com.tupack.palletsortingapi.order.domain.Pallet;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PalletRepository  extends JpaRepository<Pallet, String> {
}
