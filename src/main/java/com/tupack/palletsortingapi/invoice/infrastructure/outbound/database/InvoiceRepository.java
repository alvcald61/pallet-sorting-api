package com.tupack.palletsortingapi.invoice.infrastructure.outbound.database;

import com.tupack.palletsortingapi.invoice.application.dto.InvoiceBalanceDto;
import com.tupack.palletsortingapi.invoice.domain.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Long>,
    JpaSpecificationExecutor<Invoice> {

    boolean existsByInvoiceNumber(String invoiceNumber);

    Page<Invoice> findAllByClientId(Long clientId, Pageable pageable);

    @Query("""
        SELECT new com.tupack.palletsortingapi.invoice.application.dto.InvoiceBalanceDto(
          COALESCE(SUM(i.total), 0),
          COALESCE(SUM(CASE WHEN i.status = 'PAID' THEN i.total ELSE 0 END), 0),
          COALESCE(SUM(CASE WHEN i.status = 'PENDING' THEN i.total ELSE 0 END), 0)
        )
        FROM Invoice i WHERE i.client.id = :clientId
        """)
    InvoiceBalanceDto computeBalance(@Param("clientId") Long clientId);
}
