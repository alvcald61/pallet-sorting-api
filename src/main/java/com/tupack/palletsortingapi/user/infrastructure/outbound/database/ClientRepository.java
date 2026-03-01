package com.tupack.palletsortingapi.user.infrastructure.outbound.database;

import com.tupack.palletsortingapi.user.domain.Client;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
	Optional<Client> findClientByUserId(Long userId);

  List<Client> findAllByEnabled(boolean enabled);

  Client getClientsById(Long id);
}
