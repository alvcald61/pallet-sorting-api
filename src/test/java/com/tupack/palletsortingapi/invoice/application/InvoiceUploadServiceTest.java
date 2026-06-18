package com.tupack.palletsortingapi.invoice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tupack.palletsortingapi.base.BaseServiceTest;
import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.company.domain.Company;
import com.tupack.palletsortingapi.company.infrastructure.outbound.database.CompanyRepository;
import com.tupack.palletsortingapi.invoice.application.dto.InvoiceUploadResultDto;
import com.tupack.palletsortingapi.invoice.application.dto.InvoiceUploadResultDto.UploadStatus;
import com.tupack.palletsortingapi.invoice.application.service.InvoiceUploadService;
import com.tupack.palletsortingapi.invoice.application.service.SunatXmlParserService;
import com.tupack.palletsortingapi.invoice.domain.Invoice;
import com.tupack.palletsortingapi.invoice.domain.ParsedInvoice;
import com.tupack.palletsortingapi.invoice.infrastructure.outbound.database.InvoiceRepository;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;

@DisplayName("InvoiceUploadService Unit Tests")
class InvoiceUploadServiceTest extends BaseServiceTest {

    @Mock private SunatXmlParserService xmlParserService;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private CompanyRepository companyRepository;

    @InjectMocks
    private InvoiceUploadService service;

    private static final String SUPPLIER_RUC = "20613601296";
    private static final Company COMPANY = Company.builder()
        .id(1L).name("TUPACK").ruc(SUPPLIER_RUC).build();

    private static final ParsedInvoice PARSED = ParsedInvoice.builder()
        .supplierRuc(SUPPLIER_RUC)
        .invoiceNumber("E001-509")
        .issueDate(LocalDate.of(2026, 5, 5))
        .clientRuc("20101128939")
        .clientName("ACME SAC")
        .currency("PEN")
        .subtotal(new BigDecimal("932.03"))
        .igv(new BigDecimal("167.77"))
        .total(new BigDecimal("1099.80"))
        .build();

    @Test
    @DisplayName("Should return SUCCESS when file parsed and client matched")
    void shouldReturnSuccessWhenClientMatched() {
        MockMultipartFile file = xmlFile("test.xml");
        Client client = new Client();
        when(xmlParserService.parse(file)).thenReturn(PARSED);
        when(companyRepository.findByRucAndEnabledTrue(SUPPLIER_RUC)).thenReturn(Optional.of(COMPANY));
        when(invoiceRepository.existsByInvoiceNumber("E001-509")).thenReturn(false);
        when(clientRepository.findByRuc("20101128939")).thenReturn(Optional.of(client));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        List<InvoiceUploadResultDto> results = service.upload(List.of(file));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo(UploadStatus.SUCCESS);
        assertThat(results.get(0).getInvoiceNumber()).isEqualTo("E001-509");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should return WARNING when RUC not matched to any client")
    void shouldReturnWarningWhenRucNotMatched() {
        MockMultipartFile file = xmlFile("noruc.xml");
        when(xmlParserService.parse(file)).thenReturn(PARSED);
        when(companyRepository.findByRucAndEnabledTrue(SUPPLIER_RUC)).thenReturn(Optional.of(COMPANY));
        when(invoiceRepository.existsByInvoiceNumber("E001-509")).thenReturn(false);
        when(clientRepository.findByRuc("20101128939")).thenReturn(Optional.empty());
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(inv -> inv.getArgument(0));

        List<InvoiceUploadResultDto> results = service.upload(List.of(file));

        assertThat(results.get(0).getStatus()).isEqualTo(UploadStatus.WARNING);
        assertThat(results.get(0).getMessage()).contains("sin asignar");
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should return ERROR when invoice number is duplicate")
    void shouldReturnErrorWhenDuplicate() {
        MockMultipartFile file = xmlFile("dup.xml");
        when(xmlParserService.parse(file)).thenReturn(PARSED);
        when(companyRepository.findByRucAndEnabledTrue(SUPPLIER_RUC)).thenReturn(Optional.of(COMPANY));
        when(invoiceRepository.existsByInvoiceNumber("E001-509")).thenReturn(true);

        List<InvoiceUploadResultDto> results = service.upload(List.of(file));

        assertThat(results.get(0).getStatus()).isEqualTo(UploadStatus.ERROR);
        assertThat(results.get(0).getError()).contains("E001-509");
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return ERROR when parser throws BusinessException")
    void shouldReturnErrorWhenParserFails() {
        MockMultipartFile file = xmlFile("bad.xml");
        when(xmlParserService.parse(file))
            .thenThrow(new BusinessException("XML inválido", "INVALID_XML"));

        List<InvoiceUploadResultDto> results = service.upload(List.of(file));

        assertThat(results.get(0).getStatus()).isEqualTo(UploadStatus.ERROR);
        assertThat(results.get(0).getError()).contains("XML inválido");
    }

    @Test
    @DisplayName("Should process each file independently — one failure does not block others")
    void shouldProcessFilesIndependently() {
        MockMultipartFile bad = xmlFile("bad.xml");
        MockMultipartFile good = xmlFile("good.xml");
        Client client = new Client();
        when(xmlParserService.parse(bad))
            .thenThrow(new BusinessException("XML inválido", "INVALID_XML"));
        when(xmlParserService.parse(good)).thenReturn(PARSED);
        when(companyRepository.findByRucAndEnabledTrue(SUPPLIER_RUC)).thenReturn(Optional.of(COMPANY));
        when(invoiceRepository.existsByInvoiceNumber("E001-509")).thenReturn(false);
        when(clientRepository.findByRuc("20101128939")).thenReturn(Optional.of(client));
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<InvoiceUploadResultDto> results = service.upload(List.of(bad, good));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getStatus()).isEqualTo(UploadStatus.ERROR);
        assertThat(results.get(1).getStatus()).isEqualTo(UploadStatus.SUCCESS);
    }

    private MockMultipartFile xmlFile(String name) {
        return new MockMultipartFile("files", name, "application/xml", new byte[]{});
    }
}
