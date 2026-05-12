package com.tupack.palletsortingapi.invoice.application.service;

import com.tupack.palletsortingapi.common.exception.ClientNotFoundException;
import com.tupack.palletsortingapi.invoice.application.dto.InvoiceBalanceDto;
import com.tupack.palletsortingapi.invoice.application.dto.InvoiceDto;
import com.tupack.palletsortingapi.invoice.application.dto.InvoiceListItemDto;
import com.tupack.palletsortingapi.invoice.application.dto.PaymentEvidenceDto;
import com.tupack.palletsortingapi.invoice.domain.Invoice;
import com.tupack.palletsortingapi.invoice.domain.PaymentEvidence;
import com.tupack.palletsortingapi.invoice.domain.enums.InvoiceStatus;
import com.tupack.palletsortingapi.invoice.domain.exception.InvoiceNotFoundException;
import com.tupack.palletsortingapi.invoice.infrastructure.outbound.database.InvoiceRepository;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvoiceQueryService {

    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;

    public Page<InvoiceListItemDto> getInvoices(String status, Long userId,
        LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        Long clientId = null;
        if (userId != null) {
            Client client = clientRepository.findClientByUserId(userId)
                .orElseThrow(() -> new ClientNotFoundException("userId", userId));
            clientId = client.getId();
        }
        Specification<Invoice> spec = buildSpec(status, clientId, dateFrom, dateTo);
        return invoiceRepository.findAll(spec, pageable).map(this::toListItemDto);
    }

    public InvoiceDto getById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new InvoiceNotFoundException(id));
        return toDto(invoice);
    }

    public InvoiceBalanceDto getBalance(Long userId) {
        Client client = clientRepository.findClientByUserId(userId)
            .orElseThrow(() -> new ClientNotFoundException("userId", userId));
        InvoiceBalanceDto balance = invoiceRepository.computeBalance(client.getId());
        if (balance == null || balance.getTotalBilled() == null) {
            return InvoiceBalanceDto.builder()
                .totalBilled(BigDecimal.ZERO)
                .totalPaid(BigDecimal.ZERO)
                .pending(BigDecimal.ZERO)
                .build();
        }
        return balance;
    }

    public Page<InvoiceListItemDto> getClientInvoices(Long userId, Pageable pageable) {
        Client client = clientRepository.findClientByUserId(userId)
            .orElseThrow(() -> new ClientNotFoundException("userId", userId));
        return invoiceRepository.findAllByClientId(client.getId(), pageable).map(this::toListItemDto);
    }

    private Specification<Invoice> buildSpec(String status, Long clientId,
        LocalDate dateFrom, LocalDate dateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), InvoiceStatus.valueOf(status)));
            }
            if (clientId != null) {
                predicates.add(cb.equal(root.get("client").get("id"), clientId));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("issueDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("issueDate"), dateTo));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private InvoiceListItemDto toListItemDto(Invoice invoice) {
        Client client = invoice.getClient();
        return InvoiceListItemDto.builder()
            .id(invoice.getId())
            .invoiceNumber(invoice.getInvoiceNumber())
            .issueDate(invoice.getIssueDate())
            .dueDate(invoice.getDueDate())
            .clientRuc(invoice.getClientRuc())
            .clientName(invoice.getClientName())
            .currency(invoice.getCurrency())
            .total(invoice.getTotal())
            .status(invoice.getStatus())
            .userId(client != null ? client.getUser().getId() : null)
            .clientBusinessName(client != null ? client.getBusinessName() : null)
            .build();
    }

    private InvoiceDto toDto(Invoice invoice) {
        Client client = invoice.getClient();
        List<PaymentEvidenceDto> evidence = invoice.getEvidenceFiles() != null
            ? invoice.getEvidenceFiles().stream().map(this::toEvidenceDto).toList()
            : Collections.emptyList();
        return InvoiceDto.builder()
            .id(invoice.getId())
            .invoiceNumber(invoice.getInvoiceNumber())
            .issueDate(invoice.getIssueDate())
            .dueDate(invoice.getDueDate())
            .clientRuc(invoice.getClientRuc())
            .clientName(invoice.getClientName())
            .currency(invoice.getCurrency())
            .subtotal(invoice.getSubtotal())
            .igv(invoice.getIgv())
            .total(invoice.getTotal())
            .status(invoice.getStatus())
            .userId(client != null ? client.getUser().getId() : null)
            .clientBusinessName(client != null ? client.getBusinessName() : null)
            .paidAt(invoice.getPaidAt())
            .evidenceFiles(evidence)
            .build();
    }

    private PaymentEvidenceDto toEvidenceDto(PaymentEvidence e) {
        return PaymentEvidenceDto.builder()
            .id(e.getId())
            .fileUrl(e.getFileUrl())
            .fileName(e.getFileName())
            .uploadedBy(e.getUploadedBy())
            .createdAt(e.getCreatedAt())
            .build();
    }
}
