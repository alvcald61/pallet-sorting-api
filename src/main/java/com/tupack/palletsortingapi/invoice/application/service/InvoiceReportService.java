package com.tupack.palletsortingapi.invoice.application.service;

import com.tupack.palletsortingapi.invoice.domain.Invoice;
import com.tupack.palletsortingapi.invoice.domain.enums.InvoiceStatus;
import com.tupack.palletsortingapi.invoice.infrastructure.outbound.database.InvoiceRepository;
import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceReportService {

    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public byte[] generateReport(LocalDate dateFrom, LocalDate dateTo, Long companyId) {
        List<Invoice> invoices = invoiceRepository.findAll(buildSpec(dateFrom, dateTo, companyId));

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            if (invoices.isEmpty()) {
                workbook.createSheet("Sin datos").createRow(0).createCell(0)
                    .setCellValue("Sin datos para el período seleccionado");
                return toBytes(workbook);
            }

            CellStyle headerStyle    = buildHeaderStyle(workbook);
            CellStyle subHeaderStyle = buildSubHeaderStyle(workbook);
            CellStyle subtotalStyle  = buildSubtotalStyle(workbook);

            if (companyId != null) {
                String sheetName = sanitizeSheetName(invoices.get(0).getCompany().getName());
                writeCompanySheet(workbook.createSheet(sheetName), invoices,
                    headerStyle, subHeaderStyle, subtotalStyle);
            } else {
                Map<Long, List<Invoice>> byCompany = groupByCompany(invoices);
                for (List<Invoice> companyInvoices : byCompany.values()) {
                    String sheetName = sanitizeSheetName(companyInvoices.get(0).getCompany().getName());
                    writeCompanySheet(workbook.createSheet(sheetName), companyInvoices,
                        headerStyle, subHeaderStyle, subtotalStyle);
                }
            }

            return toBytes(workbook);
        } catch (IOException e) {
            throw new RuntimeException("Error generating invoice report", e);
        }
    }

    private void writeCompanySheet(Sheet sheet, List<Invoice> invoices,
            CellStyle headerStyle, CellStyle subHeaderStyle, CellStyle subtotalStyle) {
        Map<String, List<Invoice>> byCustomer = groupByCustomer(invoices);
        int rowNum = 0;

        for (Map.Entry<String, List<Invoice>> entry : byCustomer.entrySet()) {
            List<Invoice> group = entry.getValue();
            Invoice sample = group.get(0);

            Row customerRow = sheet.createRow(rowNum++);
            var customerCell = customerRow.createCell(0);
            customerCell.setCellValue(sample.getClientName() + " — RUC: " + sample.getClientRuc());
            customerCell.setCellStyle(headerStyle);

            Row subHeader = sheet.createRow(rowNum++);
            String[] cols = {"N° Factura", "Fecha emisión", "Vencimiento", "Moneda", "Total", "Estado"};
            for (int i = 0; i < cols.length; i++) {
                var cell = subHeader.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(subHeaderStyle);
            }

            List<Invoice> pending = group.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PENDING).toList();
            List<Invoice> paid = group.stream()
                .filter(inv -> inv.getStatus() == InvoiceStatus.PAID).toList();

            for (Invoice inv : pending) rowNum = writeInvoiceRow(sheet, rowNum, inv);
            rowNum = writeSubtotalRow(sheet, rowNum, "Total pendiente:", pending, subtotalStyle);

            for (Invoice inv : paid) rowNum = writeInvoiceRow(sheet, rowNum, inv);
            rowNum = writeSubtotalRow(sheet, rowNum, "Total pagado:", paid, subtotalStyle);

            rowNum++;
        }

        for (int i = 0; i < 6; i++) sheet.autoSizeColumn(i);
    }

    private int writeInvoiceRow(Sheet sheet, int rowNum, Invoice inv) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(inv.getInvoiceNumber());
        row.createCell(1).setCellValue(inv.getIssueDate().toString());
        row.createCell(2).setCellValue(inv.getDueDate() != null ? inv.getDueDate().toString() : "");
        row.createCell(3).setCellValue(inv.getCurrency());
        row.createCell(4).setCellValue(inv.getTotal().doubleValue());
        row.createCell(5).setCellValue(inv.getStatus().name());
        return rowNum + 1;
    }

    private int writeSubtotalRow(Sheet sheet, int rowNum, String label,
            List<Invoice> invoices, CellStyle style) {
        BigDecimal total = invoices.stream()
            .map(Invoice::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        Row row = sheet.createRow(rowNum);
        var labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);
        var totalCell = row.createCell(4);
        totalCell.setCellValue(total.doubleValue());
        totalCell.setCellStyle(style);
        return rowNum + 1;
    }

    private Map<Long, List<Invoice>> groupByCompany(List<Invoice> invoices) {
        Map<Long, List<Invoice>> map = new LinkedHashMap<>();
        for (Invoice inv : invoices) {
            map.computeIfAbsent(inv.getCompany().getId(), k -> new ArrayList<>()).add(inv);
        }
        return map;
    }

    private Map<String, List<Invoice>> groupByCustomer(List<Invoice> invoices) {
        Map<String, List<Invoice>> map = new LinkedHashMap<>();
        for (Invoice inv : invoices) {
            String key = inv.getClientName() + "|" + inv.getClientRuc();
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(inv);
        }
        return map;
    }

    private String sanitizeSheetName(String name) {
        String sanitized = name.replaceAll("[\\\\/*?\\[\\]:]", "").trim();
        return sanitized.length() > 31 ? sanitized.substring(0, 31) : sanitized;
    }

    private Specification<Invoice> buildSpec(LocalDate dateFrom, LocalDate dateTo, Long companyId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("enabled")));
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("issueDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("issueDate"), dateTo));
            }
            if (companyId != null) {
                predicates.add(cb.equal(root.get("company").get("id"), companyId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private byte[] toBytes(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }

    private CellStyle buildHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle buildSubHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle buildSubtotalStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}
