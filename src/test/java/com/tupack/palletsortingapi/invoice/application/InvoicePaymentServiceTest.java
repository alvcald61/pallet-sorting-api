package com.tupack.palletsortingapi.invoice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tupack.palletsortingapi.base.BaseServiceTest;
import com.tupack.palletsortingapi.fixtures.ClientTestFixtures;
import com.tupack.palletsortingapi.invoice.application.service.InvoicePaymentService;
import com.tupack.palletsortingapi.invoice.domain.Invoice;
import com.tupack.palletsortingapi.invoice.infrastructure.outbound.database.InvoiceRepository;
import com.tupack.palletsortingapi.invoice.infrastructure.outbound.database.PaymentEvidenceRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.storage.FileUploader;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("InvoicePaymentService Unit Tests")
class InvoicePaymentServiceTest extends BaseServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private PaymentEvidenceRepository paymentEvidenceRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private FileUploader fileUploader;
    @InjectMocks private InvoicePaymentService invoicePaymentService;

    @Test
    @DisplayName("assignClient looks up client by userId and assigns it to invoice")
    void assignClientLooksUpClientByUserId() {
        Long invoiceId = 1L;
        Long userId = 10L;
        Invoice invoice = new Invoice();
        Client client = ClientTestFixtures.createClient();
        client.setId(7L);
        client.getUser().setId(userId);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));
        when(clientRepository.findClientByUserId(userId)).thenReturn(Optional.of(client));
        when(invoiceRepository.save(invoice)).thenReturn(invoice);

        invoicePaymentService.assignClient(invoiceId, userId);

        assertThat(invoice.getClient()).isEqualTo(client);

        verify(clientRepository).findClientByUserId(userId);
        verify(invoiceRepository).save(invoice);
    }
}
