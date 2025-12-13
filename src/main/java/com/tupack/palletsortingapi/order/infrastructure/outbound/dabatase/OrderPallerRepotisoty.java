package com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase;

import com.tupack.palletsortingapi.order.domain.OrderPallet;
import com.tupack.palletsortingapi.order.domain.id.OrderPalletId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;
public interface OrderPallerRepotisoty extends JpaRepository<OrderPallet, Long> {
  List<OrderPallet> getAllByOrder_Id(Long orderId);
}