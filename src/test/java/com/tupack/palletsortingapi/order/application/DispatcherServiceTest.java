package com.tupack.palletsortingapi.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tupack.palletsortingapi.base.BaseServiceTest;
import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.fixtures.ClientTestFixtures;
import com.tupack.palletsortingapi.order.application.dto.DispatcherDto;
import com.tupack.palletsortingapi.order.application.mapper.DispatcherDtoMapper;
import com.tupack.palletsortingapi.order.application.service.DispatcherService;
import com.tupack.palletsortingapi.order.domain.Dispatcher;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.DispatcherRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("DispatcherService Unit Tests")
class DispatcherServiceTest extends BaseServiceTest {

    @Mock private DispatcherRepository dispatcherRepository;
    @Mock private DispatcherDtoMapper dispatcherDtoMapper;
    @Mock private ClientRepository clientRepository;
    @Mock private OrderRepository orderRepository;
    @InjectMocks private DispatcherService dispatcherService;

    @Test
    @DisplayName("getDispatchersByUser looks up client by userId then queries by client PK")
    void getDispatchersByUserLooksUpClientThenQueriesByClientId() {
        Long userId = 10L;
        Client client = ClientTestFixtures.createClient();
        client.setId(7L);
        client.getUser().setId(userId);

        Dispatcher dispatcher = new Dispatcher();
        DispatcherDto dto = new DispatcherDto(1L, "Ana", "Lopez", "999", userId);

        when(clientRepository.findClientByUserId(userId)).thenReturn(Optional.of(client));
        when(dispatcherRepository.findAllByEnabledAndClientId(true, 7L))
            .thenReturn(List.of(dispatcher));
        when(dispatcherDtoMapper.toDto(dispatcher)).thenReturn(dto);

        GenericResponse response = dispatcherService.getDispatchersByUser(userId);

        verify(clientRepository).findClientByUserId(userId);
        verify(dispatcherRepository).findAllByEnabledAndClientId(true, 7L);
        assertThat(response.getData()).isNotNull();
    }

    @Test
    @DisplayName("createDispatcher looks up client by userId")
    void createDispatcherLooksUpClientByUserId() {
        Long userId = 10L;
        Client client = ClientTestFixtures.createClient();
        client.setId(7L);
        client.getUser().setId(userId);

        DispatcherDto dto = new DispatcherDto(null, "Ana", "Lopez", "999", userId);
        Dispatcher entity = new Dispatcher();
        Dispatcher saved = new Dispatcher();
        saved.setId(1L);
        DispatcherDto savedDto = new DispatcherDto(1L, "Ana", "Lopez", "999", userId);

        when(clientRepository.findClientByUserId(userId)).thenReturn(Optional.of(client));
        when(dispatcherDtoMapper.toEntity(dto)).thenReturn(entity);
        when(dispatcherRepository.save(entity)).thenReturn(saved);
        when(dispatcherDtoMapper.toDto(saved)).thenReturn(savedDto);

        dispatcherService.createDispatcher(dto);

        verify(clientRepository).findClientByUserId(userId);
    }
}
