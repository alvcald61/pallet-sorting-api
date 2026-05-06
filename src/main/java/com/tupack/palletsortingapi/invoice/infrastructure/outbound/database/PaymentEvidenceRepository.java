package com.tupack.palletsortingapi.invoice.infrastructure.outbound.database;

import com.tupack.palletsortingapi.invoice.domain.PaymentEvidence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentEvidenceRepository extends JpaRepository<PaymentEvidence, Long> {
}
