package com.tupack.palletsortingapi.invoice.domain.exception;

import com.tupack.palletsortingapi.common.exception.ResourceNotFoundException;

public class InvoiceNotFoundException extends ResourceNotFoundException {

    public InvoiceNotFoundException(Long id) {
        super("Invoice", "id", id);
    }
}
