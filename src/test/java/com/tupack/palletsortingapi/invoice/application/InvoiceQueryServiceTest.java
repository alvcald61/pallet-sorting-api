package com.tupack.palletsortingapi.invoice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tupack.palletsortingapi.base.BaseServiceTest;
import com.tupack.palletsortingapi.fixtures.ClientTestFixtures;
import com.tupack.palletsortingapi.invoice.application.dto.InvoiceBalanceDto;
import com.tupack.palletsortingapi.invoice.application.service.InvoiceQueryService;
import com.tupack.palletsortingapi.invoice.infrastructure.outbound.database.InvoiceRepository;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@DisplayName("InvoiceQueryService Unit Tests")
class InvoiceQueryServiceTest extends BaseServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private ClientRepository clientRepository;
    @InjectMocks private InvoiceQueryService invoiceQueryService;

    @Test
    @DisplayName("getBalance looks up client by userId then queries by client PK")
    void getBalanceLooksUpClientByUserId() {
        Long userId = 10L;
        Client client = ClientTestFixtures.createClient();
        client.setId(7L);
        client.getUser().setId(userId);

        InvoiceBalanceDto expected = InvoiceBalanceDto.builder()
            .totalBilled(BigDecimal.valueOf(100))
            .totalPaid(BigDecimal.valueOf(50))
            .pending(BigDecimal.valueOf(50))
            .build();

        when(clientRepository.findClientByUserId(userId)).thenReturn(Optional.of(client));
        when(invoiceRepository.computeBalance(7L)).thenReturn(expected);

        InvoiceBalanceDto result = invoiceQueryService.getBalance(userId);

        verify(clientRepository).findClientByUserId(userId);
        verify(invoiceRepository).computeBalance(7L);
        assertThat(result.getTotalBilled()).isEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("getClientInvoices looks up client by userId then paginates by client PK")
    void getClientInvoicesLooksUpClientByUserId() {
        Long userId = 10L;
        Client client = ClientTestFixtures.createClient();
        client.setId(7L);
        client.getUser().setId(userId);

        when(clientRepository.findClientByUserId(userId)).thenReturn(Optional.of(client));
        when(invoiceRepository.findAllByClientId(eq(7L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        invoiceQueryService.getClientInvoices(userId, Pageable.unpaged());

        verify(clientRepository).findClientByUserId(userId);
        verify(invoiceRepository).findAllByClientId(eq(7L), any(Pageable.class));
    }
}
