package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.OrderPallet;
import com.tupack.palletsortingapi.order.domain.id.OrderPalletId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderPalletRepository extends JpaRepository<OrderPallet, Long> {
  List<OrderPallet> getAllByOrder_Id(Long orderId);
}
