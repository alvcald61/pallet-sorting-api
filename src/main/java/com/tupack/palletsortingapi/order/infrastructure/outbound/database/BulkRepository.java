package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.Bulk;
import java.util.List;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface BulkRepository extends JpaRepositoryImplementation<Bulk, Long> {
  List<Bulk> findAllByOrder_Id(Long orderId);
}
