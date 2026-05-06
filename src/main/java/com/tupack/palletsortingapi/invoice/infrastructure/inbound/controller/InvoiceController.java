package com.tupack.palletsortingapi.invoice.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.invoice.application.dto.InvoiceUploadResultDto;
import com.tupack.palletsortingapi.invoice.application.service.InvoicePaymentService;
import com.tupack.palletsortingapi.invoice.application.service.InvoiceQueryService;
import com.tupack.palletsortingapi.invoice.application.service.InvoiceUploadService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<List<InvoiceUploadResultDto>> upload(
        @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(uploadService.upload(files));
    }

    @GetMapping
    public GenericResponse getInvoices(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long clientId,
        @RequestParam(required = false) String dateFrom,
        @RequestParam(required = false) String dateTo,
        Pageable pageable) {
        LocalDate from = dateFrom != null ? LocalDate.parse(dateFrom) : null;
        LocalDate to = dateTo != null ? LocalDate.parse(dateTo) : null;
        return GenericResponse.success(queryService.getInvoices(status, clientId, from, to, pageable));
    }

    @GetMapping("/{id}")
    public GenericResponse getById(@PathVariable Long id) {
        return GenericResponse.success(queryService.getById(id));
    }

    @PatchMapping("/{id}/client")
    public GenericResponse assignClient(
        @PathVariable Long id,
        @RequestParam Long clientId) {
        paymentService.assignClient(id, clientId);
        return GenericResponse.success("Cliente asignado correctamente");
    }

    @PostMapping(value = "/{id}/pay", consumes = "multipart/form-data")
    public GenericResponse pay(
        @PathVariable Long id,
        @RequestParam("files") List<MultipartFile> files,
        Authentication authentication) {
        paymentService.markAsPaid(id, files, authentication.getName());
        return GenericResponse.success("Factura marcada como pagada");
    }

    @GetMapping("/balance/{clientId}")
    public GenericResponse getBalance(@PathVariable Long clientId) {
        return GenericResponse.success(queryService.getBalance(clientId));
    }

    @GetMapping("/client/{clientId}")
    public GenericResponse getClientInvoices(@PathVariable Long clientId, Pageable pageable) {
        return GenericResponse.success(queryService.getClientInvoices(clientId, pageable));
    }
}
