package com.tupack.palletsortingapi.order.infrastructure.outbound.database;

import com.tupack.palletsortingapi.order.domain.Dispatcher;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispatcherRepository extends JpaRepository<Dispatcher, Long> {
  List<Dispatcher> findAllByEnabledAndClientId(boolean enabled, Long clientId);
}
