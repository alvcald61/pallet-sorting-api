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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
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

        List<PaymentEvidence> evidenceList = evidenceFiles.stream()
            .map(file -> uploadEvidence(file, invoice, uploadedBy))
            .toList();

        paymentEvidenceRepository.saveAll(evidenceList);

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
    }

    @Transactional
    public void assignClient(Long invoiceId, Long userId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new InvoiceNotFoundException(invoiceId));
        Client client = clientRepository.findClientByUserId(userId)
            .orElseThrow(() -> new BusinessException(
                "Cliente no encontrado con userId: " + userId, "CLIENT_NOT_FOUND"));
        invoice.setClient(client);
        invoiceRepository.save(invoice);
    }

    private PaymentEvidence uploadEvidence(MultipartFile file, Invoice invoice, String uploadedBy) {
        try {
            String url = fileUploader.upload(file.getOriginalFilename(), file.getBytes());
            return PaymentEvidence.builder()
                .invoice(invoice)
                .fileUrl(url)
                .fileName(file.getOriginalFilename())
                .uploadedBy(uploadedBy)
                .build();
        } catch (IOException e) {
            throw new BusinessException(
                "Error al procesar el archivo: " + file.getOriginalFilename(),
                "FILE_PROCESS_ERROR");
        }
    }
}
