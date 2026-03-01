package com.tupack.palletsortingapi.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tupack.palletsortingapi.base.BaseServiceTest;
import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.common.exception.ClientNotFoundException;
import com.tupack.palletsortingapi.common.exception.DuplicateEmailException;
import com.tupack.palletsortingapi.fixtures.ClientTestFixtures;
import com.tupack.palletsortingapi.user.application.dto.CreateClientRequest;
import com.tupack.palletsortingapi.user.application.mapper.ClientMapper;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.RoleRepository;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for ClientService
 * Verifies exception handling for duplicate emails and disabled clients
 */
@DisplayName("ClientService Unit Tests")
class ClientServiceTest extends BaseServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClientService clientService;

    @Test
    @DisplayName("Should throw DuplicateEmailException when email already exists")
    void shouldThrowDuplicateEmailExceptionWhenEmailExists() {
        // Given: Email already exists
        CreateClientRequest request = new CreateClientRequest();
        request.setEmail("existing@test.com");
        request.setRoles(List.of(1L));

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When/Then: Should throw DuplicateEmailException
        assertThatThrownBy(() -> clientService.createClient(request))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("existing@test.com");

        // Should not attempt to save
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessException when updating disabled client")
    void shouldThrowBusinessExceptionWhenUpdatingDisabledClient() {
        // Given: Disabled client exists
        Long clientId = 1L;
        Client disabledClient = ClientTestFixtures.createDisabledClient();
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(disabledClient));

        // When/Then: Should throw BusinessException
        assertThatThrownBy(() -> clientService.updateClient(clientId, new CreateClientRequest()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Client is disabled");
    }

    @Test
    @DisplayName("Should throw ClientNotFoundException when client not found")
    void shouldThrowClientNotFoundExceptionWhenNotFound() {
        // Given: Client doesn't exist
        Long clientId = 999L;
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // When/Then: Should throw ClientNotFoundException
        assertThatThrownBy(() -> clientService.updateClient(clientId, new CreateClientRequest()))
            .isInstanceOf(ClientNotFoundException.class);
    }

    @Test
    @DisplayName("Should filter disabled clients when getting by ID")
    void shouldFilterDisabledClientsWhenGettingById() {
        // Given: Client exists but is disabled
        Long clientId = 1L;
        Client client = ClientTestFixtures.createDisabledClient();
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        // When/Then: Should throw exception for disabled client
        assertThatThrownBy(() -> clientService.getClientById(clientId))
            .isInstanceOf(ClientNotFoundException.class);
    }
}
