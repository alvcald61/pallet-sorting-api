package com.tupack.palletsortingapi.invoice.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParsedInvoice {
    private String invoiceNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String clientRuc;
    private String clientName;
    private String currency;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
}
