package com.tupack.palletsortingapi.invoice.application.service;

import com.tupack.palletsortingapi.invoice.domain.Invoice;
import com.tupack.palletsortingapi.invoice.domain.enums.InvoiceStatus;
import com.tupack.palletsortingapi.invoice.infrastructure.outbound.database.InvoiceRepository;
import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceReportService {

    private final InvoiceRepository invoiceRepository;

    private static final int COL_COUNT = 7;

    @Transactional(readOnly = true)
    public byte[] generateReport(LocalDate dateFrom, LocalDate dateTo, Long companyId) {
        List<Invoice> invoices = invoiceRepository.findAll(buildSpec(dateFrom, dateTo, companyId));

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            if (invoices.isEmpty()) {
                workbook.createSheet("Sin datos").createRow(0).createCell(0)
                    .setCellValue("Sin datos para el período seleccionado");
                return toBytes(workbook);
            }

            CellStyle subHeaderStyle   = buildSubHeaderStyle(workbook);
            CellStyle subtotalStyle    = buildSubtotalStyle(workbook);
            CellStyle subtotalNumStyle = buildSubtotalNumberStyle(workbook);
            CellStyle numberStyle      = buildNumberStyle(workbook);

            List<Invoice> pending = invoices.stream().filter(i -> i.getStatus() != InvoiceStatus.PAID).toList();
            List<Invoice> paid    = invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.PAID).toList();
            if (!pending.isEmpty())
                writeStatusSheet(workbook.createSheet("Pendientes"), pending, dateFrom, dateTo,
                    subHeaderStyle, subtotalStyle, subtotalNumStyle, numberStyle);
            if (!paid.isEmpty())
                writeStatusSheet(workbook.createSheet("Pagadas"), paid, dateFrom, dateTo,
                    subHeaderStyle, subtotalStyle, subtotalNumStyle, numberStyle);

            return toBytes(workbook);
        } catch (IOException e) {
            throw new RuntimeException("Error generating invoice report", e);
        }
    }

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private void writeStatusSheet(Sheet sheet, List<Invoice> invoices,
            LocalDate dateFrom, LocalDate dateTo,
            CellStyle subHeaderStyle, CellStyle subtotalStyle, CellStyle subtotalNumStyle, CellStyle numberStyle) {
        int rowNum = 0;

        CellStyle boldStyle = buildBoldStyle((XSSFWorkbook) sheet.getWorkbook());
        String from = (dateFrom != null ? dateFrom
            : invoices.stream().map(Invoice::getIssueDate).min(Comparator.naturalOrder()).orElse(LocalDate.now())).format(DATE_FMT);
        String to   = (dateTo != null ? dateTo
            : invoices.stream().map(Invoice::getIssueDate).max(Comparator.naturalOrder()).orElse(LocalDate.now())).format(DATE_FMT);
        Row titleRow = sheet.createRow(rowNum++);
        var titleCell = titleRow.createCell(0);
        titleCell.setCellValue("REPORTE DE PAGOS :    Del :    " + from + "    Al :    " + to);
        titleCell.setCellStyle(boldStyle);

        CellStyle companyNameStyle = buildCompanyNameStyle((XSSFWorkbook) sheet.getWorkbook());
        String[] cols = {"N° Factura", "Fecha emisión", "Vencimiento", "Moneda", "Total", "Días vencidos", "Estado"};

        Map<Long, List<Invoice>> byCompany = groupByCompany(invoices);
        for (List<Invoice> companyInvoices : byCompany.values()) {
            sheet.createRow(rowNum++); // blank before each company

            Row companyRow = sheet.createRow(rowNum++);
            var companyCell = companyRow.createCell(0);
            companyCell.setCellValue(companyInvoices.get(0).getCompany().getName());
            companyCell.setCellStyle(companyNameStyle);

            Row subHeader = sheet.createRow(rowNum++);
            for (int i = 0; i < cols.length; i++) {
                var cell = subHeader.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(subHeaderStyle);
            }

            Map<String, List<Invoice>> byCustomer = groupByCustomer(companyInvoices);
            for (Map.Entry<String, List<Invoice>> entry : byCustomer.entrySet()) {
                List<Invoice> group = entry.getValue();
                Invoice sample = group.get(0);

                Row customerRow = sheet.createRow(rowNum++);
                var customerCell = customerRow.createCell(0);
                customerCell.setCellValue(sample.getClientName() + " — RUC: " + sample.getClientRuc());
                customerCell.setCellStyle(boldStyle);

                for (Invoice inv : group) rowNum = writeInvoiceRow(sheet, rowNum, inv, numberStyle);
                rowNum = writeSubtotalRow(sheet, rowNum, "Subtotal:", group, subtotalStyle, subtotalNumStyle);
            }

            rowNum = writeSubtotalRow(sheet, rowNum,
                "TOTAL " + companyInvoices.get(0).getCompany().getName() + ":",
                companyInvoices, subtotalStyle, subtotalNumStyle);

            sheet.createRow(rowNum++); // blank between companies
        }

        for (int i = 0; i < COL_COUNT; i++) sheet.autoSizeColumn(i);
    }

    private int writeInvoiceRow(Sheet sheet, int rowNum, Invoice inv, CellStyle numberStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(inv.getInvoiceNumber());
        row.createCell(1).setCellValue(inv.getIssueDate().toString());
        row.createCell(2).setCellValue(inv.getDueDate() != null ? inv.getDueDate().toString() : "");
        row.createCell(3).setCellValue(inv.getCurrency());
        var totalCell = row.createCell(4);
        totalCell.setCellValue(inv.getTotal().doubleValue());
        totalCell.setCellStyle(numberStyle);
        if (inv.getDueDate() != null && LocalDate.now().isAfter(inv.getDueDate())) {
            row.createCell(5).setCellValue(ChronoUnit.DAYS.between(inv.getDueDate(), LocalDate.now()));
        }
        row.createCell(6).setCellValue(inv.getStatus() == InvoiceStatus.PAID ? "Pagado" : "Pendiente");
        return rowNum + 1;
    }

    private int writeSubtotalRow(Sheet sheet, int rowNum, String label,
            List<Invoice> invoices, CellStyle subtotalStyle, CellStyle subtotalNumStyle) {
        BigDecimal total = invoices.stream()
            .map(Invoice::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < COL_COUNT; i++) {
            var cell = row.createCell(i);
            if (i == 0) cell.setCellValue(label);
            if (i == 4) {
                cell.setCellValue(total.doubleValue());
                cell.setCellStyle(subtotalNumStyle);
            } else {
                cell.setCellStyle(subtotalStyle);
            }
        }
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

    private XSSFColor subtotalGray() {
        return new XSSFColor(new byte[]{(byte) 231, (byte) 230, (byte) 230}, null);
    }

    private CellStyle buildSubtotalStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(subtotalGray());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle buildSubtotalNumberStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(subtotalGray());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        DataFormat fmt = workbook.createDataFormat();
        style.setDataFormat(fmt.getFormat("#,##0.00"));
        return style;
    }

    private CellStyle buildCompanyNameStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private CellStyle buildBoldStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle buildNumberStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat fmt = workbook.createDataFormat();
        style.setDataFormat(fmt.getFormat("#,##0.00"));
        return style;
    }
}
