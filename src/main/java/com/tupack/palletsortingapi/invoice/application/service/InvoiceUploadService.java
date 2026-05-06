package com.tupack.palletsortingapi.invoice.application.service;

import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.invoice.application.dto.InvoiceUploadResultDto;
import com.tupack.palletsortingapi.invoice.application.dto.InvoiceUploadResultDto.UploadStatus;
import com.tupack.palletsortingapi.invoice.domain.Invoice;
import com.tupack.palletsortingapi.invoice.domain.ParsedInvoice;
import com.tupack.palletsortingapi.invoice.domain.enums.InvoiceStatus;
import com.tupack.palletsortingapi.invoice.infrastructure.outbound.database.InvoiceRepository;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceUploadService {

    private final SunatXmlParserService xmlParserService;
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public List<InvoiceUploadResultDto> upload(List<MultipartFile> files) {
        return files.stream()
            .map(this::processFile)
            .toList();
    }

    private InvoiceUploadResultDto processFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        try {
            ParsedInvoice parsed = xmlParserService.parse(file);

            if (invoiceRepository.existsByInvoiceNumber(parsed.getInvoiceNumber())) {
                return InvoiceUploadResultDto.builder()
                    .fileName(fileName)
                    .status(UploadStatus.ERROR)
                    .error("La factura " + parsed.getInvoiceNumber() + " ya existe en el sistema")
                    .build();
            }

            Optional<Client> clientOpt = clientRepository.findByRuc(parsed.getClientRuc());

            Invoice invoice = Invoice.builder()
                .invoiceNumber(parsed.getInvoiceNumber())
                .issueDate(parsed.getIssueDate())
                .dueDate(parsed.getDueDate())
                .clientRuc(parsed.getClientRuc())
                .clientName(parsed.getClientName())
                .currency(parsed.getCurrency())
                .subtotal(parsed.getSubtotal())
                .igv(parsed.getIgv())
                .total(parsed.getTotal())
                .status(InvoiceStatus.PENDING)
                .client(clientOpt.orElse(null))
                .build();

            invoiceRepository.save(invoice);

            if (clientOpt.isEmpty()) {
                return InvoiceUploadResultDto.builder()
                    .fileName(fileName)
                    .status(UploadStatus.WARNING)
                    .invoiceNumber(parsed.getInvoiceNumber())
                    .message("RUC " + parsed.getClientRuc() + " no encontrado — guardada sin asignar")
                    .build();
            }

            return InvoiceUploadResultDto.builder()
                .fileName(fileName)
                .status(UploadStatus.SUCCESS)
                .invoiceNumber(parsed.getInvoiceNumber())
                .build();

        } catch (BusinessException e) {
            return InvoiceUploadResultDto.builder()
                .fileName(fileName)
                .status(UploadStatus.ERROR)
                .error(e.getMessage())
                .build();
        } catch (Exception e) {
            log.error("Unexpected error processing file '{}': {}", fileName, e.getMessage(), e);
            return InvoiceUploadResultDto.builder()
                .fileName(fileName)
                .status(UploadStatus.ERROR)
                .error("Error inesperado al procesar el archivo")
                .build();
        }
    }
}
