package com.tupack.palletsortingapi.invoice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tupack.palletsortingapi.base.BaseServiceTest;
import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.invoice.application.service.SunatXmlParserService;
import com.tupack.palletsortingapi.invoice.domain.ParsedInvoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("SunatXmlParserService Unit Tests")
class SunatXmlParserServiceTest extends BaseServiceTest {

    @InjectMocks
    private SunatXmlParserService service;

    private static final String COMPANY_RUC = "20613601296";

    private static final String VALID_XML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"
                 xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
                 xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2">
          <cbc:ID>E001-509</cbc:ID>
          <cbc:IssueDate>2026-05-05</cbc:IssueDate>
          <cbc:DocumentCurrencyCode>PEN</cbc:DocumentCurrencyCode>
          <cac:AccountingSupplierParty>
            <cac:Party>
              <cac:PartyIdentification>
                <cbc:ID schemeID="6">20613601296</cbc:ID>
              </cac:PartyIdentification>
            </cac:Party>
          </cac:AccountingSupplierParty>
          <cac:AccountingCustomerParty>
            <cac:Party>
              <cac:PartyIdentification>
                <cbc:ID schemeID="6">20101128939</cbc:ID>
              </cac:PartyIdentification>
              <cac:PartyLegalEntity>
                <cbc:RegistrationName>AGENTES PROFESIONALES DE ADUANAS S.A.C.</cbc:RegistrationName>
              </cac:PartyLegalEntity>
            </cac:Party>
          </cac:AccountingCustomerParty>
          <cac:PaymentTerms>
            <cbc:ID>FormaPago</cbc:ID>
            <cbc:PaymentMeansID>Cuota001</cbc:PaymentMeansID>
            <cbc:PaymentDueDate>2026-05-19</cbc:PaymentDueDate>
          </cac:PaymentTerms>
          <cac:TaxTotal>
            <cbc:TaxAmount currencyID="PEN">167.77</cbc:TaxAmount>
          </cac:TaxTotal>
          <cac:LegalMonetaryTotal>
            <cbc:LineExtensionAmount currencyID="PEN">932.03</cbc:LineExtensionAmount>
            <cbc:PayableAmount currencyID="PEN">1099.80</cbc:PayableAmount>
          </cac:LegalMonetaryTotal>
        </Invoice>
        """;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "companyRuc", COMPANY_RUC);
    }

    @Test
    @DisplayName("Should parse valid SUNAT XML into ParsedInvoice")
    void shouldParseValidXml() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.xml",
            "application/xml", VALID_XML.getBytes());

        ParsedInvoice result = service.parse(file);

        assertThat(result.getInvoiceNumber()).isEqualTo("E001-509");
        assertThat(result.getClientRuc()).isEqualTo("20101128939");
        assertThat(result.getClientName()).isEqualTo("AGENTES PROFESIONALES DE ADUANAS S.A.C.");
        assertThat(result.getCurrency()).isEqualTo("PEN");
        assertThat(result.getSubtotal()).isEqualByComparingTo("932.03");
        assertThat(result.getIgv()).isEqualByComparingTo("167.77");
        assertThat(result.getTotal()).isEqualByComparingTo("1099.80");
        assertThat(result.getDueDate()).isNotNull();
    }

    @Test
    @DisplayName("Should throw BusinessException when supplier RUC does not match company RUC")
    void shouldRejectWrongSupplierRuc() {
        String wrongRucXml = VALID_XML.replace("<cbc:ID schemeID=\"6\">20613601296</cbc:ID>",
            "<cbc:ID schemeID=\"6\">99999999999</cbc:ID>");
        MockMultipartFile file = new MockMultipartFile("file", "test.xml",
            "application/xml", wrongRucXml.getBytes());

        assertThatThrownBy(() -> service.parse(file))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("emitida por esta empresa");
    }

    @Test
    @DisplayName("Should throw BusinessException when required field is missing")
    void shouldThrowWhenRequiredFieldMissing() {
        String noIdXml = VALID_XML.replace("<cbc:ID>E001-509</cbc:ID>", "");
        MockMultipartFile file = new MockMultipartFile("file", "test.xml",
            "application/xml", noIdXml.getBytes());

        assertThatThrownBy(() -> service.parse(file))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Should throw BusinessException for malformed XML")
    void shouldThrowForMalformedXml() {
        MockMultipartFile file = new MockMultipartFile("file", "bad.xml",
            "application/xml", "not xml content".getBytes());

        assertThatThrownBy(() -> service.parse(file))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("válido");
    }
}
