package com.tupack.palletsortingapi.invoice.application.dto;

import com.tupack.palletsortingapi.invoice.domain.enums.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceDto {
    private Long id;
    private String invoiceNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String clientRuc;
    private String clientName;
    private String currency;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private InvoiceStatus status;
    private Long clientId;
    private String clientBusinessName;
    private LocalDateTime paidAt;
    private List<PaymentEvidenceDto> evidenceFiles;
}
