package com.tupack.palletsortingapi.order.application;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tupack.palletsortingapi.base.BaseServiceTest;
import com.tupack.palletsortingapi.fixtures.ClientTestFixtures;
import com.tupack.palletsortingapi.order.application.dto.PriceDto;
import com.tupack.palletsortingapi.order.application.mapper.PriceConditionDtoMapper;
import com.tupack.palletsortingapi.order.application.mapper.PriceDtoMapper;
import com.tupack.palletsortingapi.order.domain.Price;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceConditionRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.ZoneRepository;
import com.tupack.palletsortingapi.order.application.service.PriceService;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("PriceService Unit Tests")
class PriceServiceTest extends BaseServiceTest {

    @Mock private PriceConditionRepository priceConditionRepository;
    @Mock private PriceConditionDtoMapper priceConditionDtoMapper;
    @Mock private ZoneRepository zoneRepository;
    @Mock private PriceDtoMapper priceDtoMapper;
    @Mock private PriceRepository priceRepository;
    @Mock private ClientRepository clientRepository;
    @InjectMocks private PriceService priceService;

    @Test
    @DisplayName("getAllPrices with userId looks up client then filters by client PK")
    void getAllPricesWithUserIdLooksUpClientByUserId() {
        Long userId = 10L;
        Client client = ClientTestFixtures.createClient();
        client.setId(7L);
        client.getUser().setId(userId);

        when(clientRepository.findClientByUserId(userId)).thenReturn(Optional.of(client));
        when(priceRepository.findAllByEnabledAndClientId(true, 7L)).thenReturn(List.of());

        priceService.getAllPrices(userId);

        verify(clientRepository).findClientByUserId(userId);
        verify(priceRepository).findAllByEnabledAndClientId(true, 7L);
    }

    @Test
    @DisplayName("getAllPrices with null userId returns all prices without client lookup")
    void getAllPricesWithNullUserIdReturnsAll() {
        when(priceRepository.findAllByEnabled(true)).thenReturn(List.of(new Price()));
        when(priceDtoMapper.toDto(org.mockito.ArgumentMatchers.any())).thenReturn(
            new PriceDto(null, null, null, null, null, BigDecimal.ZERO, null, null, null, null, true));

        priceService.getAllPrices(null);

        verify(priceRepository).findAllByEnabled(true);
    }

    @Test
    @DisplayName("createPrice with userId resolves client via findClientByUserId")
    void createPriceWithUserIdResolvesClient() {
        Long userId = 10L;
        Client client = ClientTestFixtures.createClient();
        client.setId(7L);
        client.getUser().setId(userId);

        PriceDto dto = new PriceDto(null, null, null, userId, null, BigDecimal.TEN,
            null, null, null, null, true);
        Price entity = new Price();

        when(clientRepository.findClientByUserId(userId)).thenReturn(Optional.of(client));
        when(priceDtoMapper.toEntity(dto)).thenReturn(entity);
        when(priceRepository.save(entity)).thenReturn(entity);
        when(priceDtoMapper.toDto(entity)).thenReturn(dto);

        priceService.createPrice(dto);

        verify(clientRepository).findClientByUserId(userId);
    }
}
