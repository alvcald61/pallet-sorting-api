package com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase;

import com.tupack.palletsortingapi.order.domain.Pallet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PalletRepository  extends JpaRepository<Pallet, Long> {
  List<Pallet> findAllByEnabled(boolean enabled);

  Optional<Pallet> findByWidthAndLengthAndHeight(Double width, Double length, Double height);
}
