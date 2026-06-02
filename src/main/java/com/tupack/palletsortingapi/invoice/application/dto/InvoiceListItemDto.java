package com.tupack.palletsortingapi.invoice.application.dto;

import com.tupack.palletsortingapi.company.application.dto.CompanyDto;
import com.tupack.palletsortingapi.invoice.domain.enums.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceListItemDto {
    private Long id;
    private Long clientId;
    private String invoiceNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String clientRuc;
    private String clientName;
    private String currency;
    private BigDecimal total;
    private InvoiceStatus status;
    private Long userId;
    private String clientBusinessName;
    private CompanyDto company;
}
