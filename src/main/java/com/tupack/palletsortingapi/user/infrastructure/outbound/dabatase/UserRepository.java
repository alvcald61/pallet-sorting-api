package com.tupack.palletsortingapi.user.infrastructure.outbound.dabatase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tupack.palletsortingapi.user.domain.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByEmail(String email);

  Optional<User> findByEmail(String email);
}
