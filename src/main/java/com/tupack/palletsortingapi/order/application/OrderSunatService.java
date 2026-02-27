package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.enums.OrderStatus;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSunatService {

    private static final String DOCUMENT_DIR = "documento";
    private static final java.util.List<OrderStatus> ALLOWED_STATUSES = java.util.List.of(
            OrderStatus.PRE_APPROVED,
            OrderStatus.APPROVED
    );

    private final OrderRepository orderRepository;

    /**
     * Upload the SUNAT document for an order.
     * The file is saved to ./documento/sunat_{orderId}.pdf.
     * Only allowed when order is in PRE_APPROVED or APPROVED status.
     */
    @Transactional
    public GenericResponse uploadSunatDocument(Long orderId, MultipartFile file) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + orderId));

        if (!ALLOWED_STATUSES.contains(order.getOrderStatus())) {
            throw new IllegalStateException(
                    "El documento SUNAT solo puede subirse cuando la orden está en estado PRE_APPROVED o APPROVED. " +
                    "Estado actual: " + order.getOrderStatus());
        }

        try {
            Path documentDir = Paths.get(DOCUMENT_DIR);
            if (!Files.exists(documentDir)) {
                Files.createDirectories(documentDir);
                log.info("Created document directory: {}", documentDir.toAbsolutePath());
            }

            String fileName = "sunat_" + orderId + ".pdf";
            Path targetPath = documentDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("SUNAT document saved: {}", targetPath.toAbsolutePath());

            order.setSunatDocumentPath(DOCUMENT_DIR + "/" + fileName);
            orderRepository.save(order);

            return GenericResponse.builder()
                    .statusCode(200)
                    .message("Documento SUNAT guardado correctamente")
                    .data(order.getSunatDocumentPath())
                    .build();

        } catch (IOException e) {
            log.error("Error saving SUNAT document for order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Error al guardar el documento SUNAT: " + e.getMessage(), e);
        }
    }

    /**
     * Get the SUNAT document as a Resource for download.
     */
    public Resource getSunatDocument(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + orderId));

        if (order.getSunatDocumentPath() == null) {
            throw new IllegalStateException("La orden " + orderId + " no tiene documento SUNAT");
        }

        try {
            Path filePath = Paths.get(order.getSunatDocumentPath()).toAbsolutePath();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("El archivo no se puede leer: " + filePath);
            }
            return resource;
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException("Error de URL al leer el documento SUNAT", e);
        }
    }
}
