package com.tupack.palletsortingapi.invoice.application.service;

import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.invoice.domain.ParsedInvoice;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

@Service
@Slf4j
public class SunatXmlParserService {

    private static final Map<String, String> NS = Map.of(
        "cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2",
        "cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2",
        "inv", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"
    );

    @Value("${application.company.ruc}")
    private String companyRuc;

    public ParsedInvoice parse(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            Document doc = buildDocument(bytes);
            XPath xpath = buildXPath();

            String supplierRuc = eval(xpath, doc,
                "//cac:AccountingSupplierParty//cbc:ID[@schemeID='6']");
            if (!companyRuc.equals(supplierRuc.trim())) {
                throw new BusinessException(
                    "La factura no fue emitida por esta empresa", "SUPPLIER_RUC_MISMATCH");
            }

            String invoiceNumber = requireField(xpath, doc, "/inv:Invoice/cbc:ID", "cbc:ID");
            LocalDate issueDate = LocalDate.parse(
                requireField(xpath, doc, "/inv:Invoice/cbc:IssueDate", "cbc:IssueDate"));
            String dueDateStr = eval(xpath, doc,
                "//cac:PaymentTerms[cbc:PaymentMeansID='Cuota001']/cbc:PaymentDueDate");
            LocalDate dueDate = (dueDateStr == null || dueDateStr.isBlank())
                ? null : LocalDate.parse(dueDateStr.trim());
            String clientRuc = requireField(xpath, doc,
                "//cac:AccountingCustomerParty//cbc:ID[@schemeID='6']", "clientRuc");
            String clientName = requireField(xpath, doc,
                "//cac:AccountingCustomerParty//cbc:RegistrationName", "clientName");
            String currency = requireField(xpath, doc,
                "/inv:Invoice/cbc:DocumentCurrencyCode", "currency");
            BigDecimal subtotal = parseBigDecimal(requireField(xpath, doc,
                "//cac:LegalMonetaryTotal/cbc:LineExtensionAmount", "subtotal"));
            BigDecimal igv = parseBigDecimal(requireField(xpath, doc,
                "//cac:TaxTotal/cbc:TaxAmount", "igv"));
            BigDecimal total = parseBigDecimal(requireField(xpath, doc,
                "//cac:LegalMonetaryTotal/cbc:PayableAmount", "total"));

            return ParsedInvoice.builder()
                .invoiceNumber(invoiceNumber)
                .issueDate(issueDate)
                .dueDate(dueDate)
                .clientRuc(clientRuc.trim())
                .clientName(clientName.trim())
                .currency(currency.trim())
                .subtotal(subtotal)
                .igv(igv)
                .total(total)
                .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to parse SUNAT XML file '{}': {}", file.getOriginalFilename(),
                e.getMessage());
            throw new BusinessException(
                "El archivo no es un XML de factura válido: " + e.getMessage(), "INVALID_XML");
        }
    }

    private Document buildDocument(byte[] bytes) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(bytes));
    }

    private XPath buildXPath() {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new SimpleNamespaceContext(NS));
        return xpath;
    }

    private String eval(XPath xpath, Document doc, String expression) throws Exception {
        return xpath.evaluate(expression, doc);
    }

    private String requireField(XPath xpath, Document doc, String expression, String fieldName)
        throws Exception {
        String value = eval(xpath, doc, expression);
        if (value == null || value.isBlank()) {
            throw new BusinessException(
                "Faltan campos obligatorios: " + fieldName, "MISSING_FIELD");
        }
        return value.trim();
    }

    private BigDecimal parseBigDecimal(String value) {
        return new BigDecimal(value.trim());
    }
}
