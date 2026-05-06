package com.tupack.palletsortingapi.invoice.application.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentEvidenceDto {
    private Long id;
    private String fileUrl;
    private String fileName;
    private String uploadedBy;
    private LocalDateTime createdAt;
}
