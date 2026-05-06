package com.tupack.palletsortingapi.invoice.application.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceBalanceDto {
    private BigDecimal totalBilled;
    private BigDecimal totalPaid;
    private BigDecimal pending;
}
