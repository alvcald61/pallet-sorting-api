package com.tupack.palletsortingapi.invoice.domain.exception;

public class InvoiceAlreadyPaidException extends IllegalStateException {

    public InvoiceAlreadyPaidException(Long invoiceId) {
        super("La factura con id " + invoiceId + " ya fue pagada");
    }
}
