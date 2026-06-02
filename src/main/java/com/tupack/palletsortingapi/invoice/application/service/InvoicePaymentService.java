package com.tupack.palletsortingapi.invoice.application.service;

import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.invoice.domain.Invoice;
import com.tupack.palletsortingapi.invoice.domain.PaymentEvidence;
import com.tupack.palletsortingapi.invoice.domain.enums.InvoiceStatus;
import com.tupack.palletsortingapi.invoice.domain.exception.InvoiceAlreadyPaidException;
import com.tupack.palletsortingapi.invoice.domain.exception.InvoiceNotFoundException;
import com.tupack.palletsortingapi.invoice.infrastructure.outbound.database.InvoiceRepository;
import com.tupack.palletsortingapi.invoice.infrastructure.outbound.database.PaymentEvidenceRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.storage.FileUploader;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class InvoicePaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentEvidenceRepository paymentEvidenceRepository;
    private final ClientRepository clientRepository;
    private final FileUploader fileUploader;

    @Transactional
    public void markAsPaid(Long invoiceId, List<MultipartFile> evidenceFiles, String uploadedBy) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidException(invoiceId);
        }
        if (evidenceFiles == null || evidenceFiles.isEmpty()) {
            throw new BusinessException(
                "Se requiere al menos un archivo de evidencia", "EVIDENCE_REQUIRED");
        }

        List<UploadedFile> uploads = evidenceFiles.stream().map(this::uploadFile).toList();
        paymentEvidenceRepository.saveAll(
            uploads.stream().map(u -> buildEvidence(u, invoice, uploadedBy)).toList());
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
    }

    @Transactional
    public int markManyAsPaid(List<Long> invoiceIds, List<MultipartFile> evidenceFiles, String uploadedBy) {
        if (evidenceFiles == null || evidenceFiles.isEmpty()) {
            throw new BusinessException(
                "Se requiere al menos un archivo de evidencia", "EVIDENCE_REQUIRED");
        }

        List<UploadedFile> uploads = evidenceFiles.stream().map(this::uploadFile).toList();

        int count = 0;
        for (Long id : invoiceIds) {
            Optional<Invoice> opt = invoiceRepository.findById(id);
            if (opt.isEmpty() || !opt.get().isEnabled()
                    || opt.get().getStatus() == InvoiceStatus.PAID) {
                continue;
            }
            Invoice invoice = opt.get();
            paymentEvidenceRepository.saveAll(
                uploads.stream().map(u -> buildEvidence(u, invoice, uploadedBy)).toList());
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
            count++;
        }
        return count;
    }

    @Transactional
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new InvoiceNotFoundException(id));
        invoice.setEnabled(false);
        invoiceRepository.save(invoice);
    }

    @Transactional
    public void assignClient(Long invoiceId, Long userId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));
        Client client = clientRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(
                "Cliente no encontrado con userId: " + userId, "CLIENT_NOT_FOUND"));
        invoice.setClient(client);
        invoiceRepository.save(invoice);
    }

    private record UploadedFile(String url, String fileName) {}

    private UploadedFile uploadFile(MultipartFile file) {
        try {
            String url = fileUploader.upload(file.getOriginalFilename(), file.getBytes());
            return new UploadedFile(url, file.getOriginalFilename());
        } catch (IOException e) {
            throw new BusinessException(
                "Error al procesar el archivo: " + file.getOriginalFilename(),
                "FILE_PROCESS_ERROR", e);
        }
    }

    private PaymentEvidence buildEvidence(UploadedFile upload, Invoice invoice, String uploadedBy) {
        return PaymentEvidence.builder()
            .invoice(invoice)
            .fileUrl(upload.url())
            .fileName(upload.fileName())
            .uploadedBy(uploadedBy)
            .build();
    }
}
