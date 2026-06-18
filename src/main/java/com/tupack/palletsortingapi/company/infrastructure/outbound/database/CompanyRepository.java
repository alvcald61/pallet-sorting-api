package com.tupack.palletsortingapi.company.infrastructure.outbound.database;

import com.tupack.palletsortingapi.company.domain.Company;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByRuc(String ruc);

    Optional<Company> findByRucAndEnabledTrue(String ruc);

    List<Company> findAllByEnabledTrue();
}
