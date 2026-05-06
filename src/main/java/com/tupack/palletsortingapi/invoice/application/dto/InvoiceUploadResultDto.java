package com.tupack.palletsortingapi.invoice.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvoiceUploadResultDto {

    public enum UploadStatus { SUCCESS, WARNING, ERROR }

    private String fileName;
    private UploadStatus status;
    private String invoiceNumber;
    private String error;
    private String message;
}
