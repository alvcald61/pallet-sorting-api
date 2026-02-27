package com.tupack.palletsortingapi.order.infrastructure.inbound.controller;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.OrderSunatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderSunatController {

    private final OrderSunatService orderSunatService;

    /**
     * POST /api/order/{orderId}/sunat-document
     * Upload the SUNAT document for an approved order (admin only).
     * The file is saved to ./documento/sunat_{orderId}.pdf on the server.
     */
    @PostMapping(value = "/{orderId}/sunat-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericResponse> uploadSunatDocument(
            @PathVariable Long orderId,
            @RequestParam("file") MultipartFile file) {
        GenericResponse response = orderSunatService.uploadSunatDocument(orderId, file);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/order/{orderId}/sunat-document
     * Download the SUNAT document for an order.
     */
    @GetMapping("/{orderId}/sunat-document")
    public ResponseEntity<Resource> downloadSunatDocument(@PathVariable Long orderId) {
        Resource resource = orderSunatService.getSunatDocument(orderId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"documento_sunat_" + orderId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
