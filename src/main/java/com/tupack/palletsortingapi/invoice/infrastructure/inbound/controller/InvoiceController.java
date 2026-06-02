package com.tupack.palletsortingapi.invoice.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.company.domain.Company;
import com.tupack.palletsortingapi.company.infrastructure.outbound.database.CompanyRepository;
import com.tupack.palletsortingapi.invoice.application.dto.InvoiceUploadResultDto;
import com.tupack.palletsortingapi.invoice.application.service.InvoicePaymentService;
import com.tupack.palletsortingapi.invoice.application.service.InvoiceQueryService;
import com.tupack.palletsortingapi.invoice.application.service.InvoiceReportService;
import com.tupack.palletsortingapi.invoice.application.service.InvoiceUploadService;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceUploadService uploadService;
    private final InvoiceQueryService queryService;
    private final InvoicePaymentService paymentService;
    private final InvoiceReportService reportService;
    private final CompanyRepository companyRepository;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<List<InvoiceUploadResultDto>> upload(
        @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(uploadService.upload(files));
    }

    @GetMapping
    public GenericResponse getInvoices(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long userId,
        @RequestParam(required = false) Long clientId,
        @RequestParam(required = false) String dateFrom,
        @RequestParam(required = false) String dateTo,
        @RequestParam(required = false) Long companyId,
        Pageable pageable) {
        LocalDate from = dateFrom != null ? LocalDate.parse(dateFrom) : null;
        LocalDate to = dateTo != null ? LocalDate.parse(dateTo) : null;
        return GenericResponse.success(queryService.getInvoices(status, userId, clientId, from, to, companyId, pageable));
    }

    @GetMapping("/{id}")
    public GenericResponse getById(@PathVariable Long id) {
        return GenericResponse.success(queryService.getById(id));
    }

    @PatchMapping("/{id}/client")
    public GenericResponse assignClient(
        @PathVariable Long id,
        @RequestParam Long userId) {
        paymentService.assignClient(id, userId);
        return GenericResponse.success("Cliente asignado correctamente");
    }

    @PostMapping(value = "/pay-bulk", consumes = "multipart/form-data")
    public GenericResponse payBulk(
        @RequestParam("invoiceIds") List<Long> invoiceIds,
        @RequestParam("files") List<MultipartFile> files,
        Authentication authentication) {
        int count = paymentService.markManyAsPaid(invoiceIds, files, authentication.getName());
        return GenericResponse.success(count + " factura(s) marcadas como pagadas");
    }

    @PostMapping(value = "/{id}/pay", consumes = "multipart/form-data")
    public GenericResponse pay(
        @PathVariable Long id,
        @RequestParam("files") List<MultipartFile> files,
        Authentication authentication) {
        paymentService.markAsPaid(id, files, authentication.getName());
        return GenericResponse.success("Factura marcada como pagada");
    }

    @GetMapping("/balance/{userId}")
    public GenericResponse getBalance(@PathVariable Long userId) {
        return GenericResponse.success(queryService.getBalance(userId));
    }

    @GetMapping("/client/{userId}")
    public GenericResponse getClientInvoices(@PathVariable Long userId, Pageable pageable) {
        return GenericResponse.success(queryService.getClientInvoices(userId, pageable));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public GenericResponse delete(@PathVariable Long id) {
        paymentService.deleteInvoice(id);
        return GenericResponse.success("Factura eliminada");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/report/export")
    public ResponseEntity<byte[]> exportReport(
        @RequestParam(required = false) String dateFrom,
        @RequestParam(required = false) String dateTo,
        @RequestParam(required = false) Long companyId) {
        LocalDate from = dateFrom != null ? LocalDate.parse(dateFrom) : null;
        LocalDate to   = dateTo   != null ? LocalDate.parse(dateTo)   : null;

        byte[] bytes = reportService.generateReport(from, to, companyId);
        String filename = buildFilename(companyId, from, to);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(bytes);
    }

    private String buildFilename(Long companyId, LocalDate from, LocalDate to) {
        StringBuilder name = new StringBuilder("reporte-facturas");
        if (companyId != null) {
            String companySlug = companyRepository.findById(companyId)
                .map(Company::getName)
                .map(this::slugify)
                .orElse("empresa");
            name.append("-").append(companySlug);
        }
        if (from != null) name.append("-").append(from);
        if (to != null)   name.append("-al-").append(to);
        name.append(".xlsx");
        return name.toString();
    }

    private String slugify(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase()
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
    }
}
