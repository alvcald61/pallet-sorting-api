package com.tupack.palletsortingapi.invoice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.tupack.palletsortingapi.base.BaseServiceTest;
import com.tupack.palletsortingapi.company.domain.Company;
import com.tupack.palletsortingapi.invoice.application.service.InvoiceReportService;
import com.tupack.palletsortingapi.invoice.domain.Invoice;
import com.tupack.palletsortingapi.invoice.domain.enums.InvoiceStatus;
import com.tupack.palletsortingapi.invoice.infrastructure.outbound.database.InvoiceRepository;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.jpa.domain.Specification;

@DisplayName("InvoiceReportService Unit Tests")
class InvoiceReportServiceTest extends BaseServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @InjectMocks private InvoiceReportService invoiceReportService;

    private static final Company COMPANY_A = Company.builder()
        .id(1L).name("TUPACK").ruc("20613601296").build();

    private static final Company COMPANY_B = Company.builder()
        .id(2L).name("XISA LOGISTICS").ruc("20609747928").build();

    @Test
    @DisplayName("generateReport returns valid XLSX bytes when invoices exist")
    void generateReportReturnsValidXlsx() throws Exception {
        when(invoiceRepository.findAll(ArgumentMatchers.<Specification<Invoice>>any()))
            .thenReturn(List.of(buildInvoice("F001-001", "Empresa A", "20111111111",
                InvoiceStatus.PAID, BigDecimal.valueOf(100), COMPANY_A)));

        byte[] result = invoiceReportService.generateReport(null, null, null);

        assertThat(result).isNotEmpty();
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertThat(wb.getNumberOfSheets()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("generateReport produces rows for each customer with paid and pending invoices")
    void generateReportGroupsCustomers() throws Exception {
        when(invoiceRepository.findAll(ArgumentMatchers.<Specification<Invoice>>any())).thenReturn(List.of(
            buildInvoice("F001-001", "Empresa A", "20111111111", InvoiceStatus.PAID,    BigDecimal.valueOf(100), COMPANY_A),
            buildInvoice("F001-002", "Empresa A", "20111111111", InvoiceStatus.PENDING, BigDecimal.valueOf(200), COMPANY_A),
            buildInvoice("F002-001", "Empresa B", "20222222222", InvoiceStatus.PENDING, BigDecimal.valueOf(50),  COMPANY_A)
        ));

        byte[] result = invoiceReportService.generateReport(
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), null);

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertThat(wb.getNumberOfSheets()).isEqualTo(1);
            // 2 customers × (customer header + col headers + invoices + 2 subtotals) + blank = > 8 rows
            assertThat(wb.getSheetAt(0).getLastRowNum()).isGreaterThan(8);
        }
    }

    @Test
    @DisplayName("generateReport creates one sheet per company when no company filter")
    void generateReportCreatesOneSheetPerCompany() throws Exception {
        when(invoiceRepository.findAll(ArgumentMatchers.<Specification<Invoice>>any())).thenReturn(List.of(
            buildInvoice("F001-001", "Cliente X", "20111111111", InvoiceStatus.PAID,    BigDecimal.valueOf(100), COMPANY_A),
            buildInvoice("F002-001", "Cliente Y", "20222222222", InvoiceStatus.PENDING, BigDecimal.valueOf(50),  COMPANY_B)
        ));

        byte[] result = invoiceReportService.generateReport(null, null, null);

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertThat(wb.getNumberOfSheets()).isEqualTo(2);
            assertThat(wb.getSheetName(0)).isEqualTo("TUPACK");
            assertThat(wb.getSheetName(1)).isEqualTo("XISA LOGISTICS");
        }
    }

    @Test
    @DisplayName("generateReport returns XLSX with a single message row when no invoices found")
    void generateReportHandlesEmptyResult() throws Exception {
        when(invoiceRepository.findAll(ArgumentMatchers.<Specification<Invoice>>any())).thenReturn(List.of());

        byte[] result = invoiceReportService.generateReport(null, null, null);

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertThat(wb.getSheetAt(0).getLastRowNum()).isEqualTo(0);
        }
    }

    private Invoice buildInvoice(String number, String clientName, String clientRuc,
            InvoiceStatus status, BigDecimal total, Company company) {
        return Invoice.builder()
            .invoiceNumber(number)
            .clientName(clientName)
            .clientRuc(clientRuc)
            .issueDate(LocalDate.of(2026, 3, 1))
            .currency("PEN")
            .subtotal(total)
            .igv(BigDecimal.ZERO)
            .total(total)
            .status(status)
            .company(company)
            .build();
    }
}
