package com.tupack.palletsortingapi.order.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tupack.palletsortingapi.base.BaseServiceTest;
import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.common.exception.PalletNotFoundException;
import com.tupack.palletsortingapi.fixtures.PalletTestFixtures;
import com.tupack.palletsortingapi.order.application.mapper.PalletMapper;
import com.tupack.palletsortingapi.order.application.service.PalletService;
import com.tupack.palletsortingapi.order.domain.Pallet;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PalletRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit tests for PalletService
 * Verifies exception handling and business logic
 */
@DisplayName("PalletService Unit Tests")
class PalletServiceTest extends BaseServiceTest {

    @Mock
    private PalletRepository palletRepository;

    @Mock
    private PalletMapper palletMapper;

    @InjectMocks
    private PalletService palletService;

    @Test
    @DisplayName("Should throw PalletNotFoundException when pallet not found")
    void shouldThrowPalletNotFoundExceptionWhenNotFound() {
        // Given: Pallet doesn't exist
        Long palletId = 999L;
        when(palletRepository.findById(palletId)).thenReturn(Optional.empty());

        // When/Then: Should throw PalletNotFoundException
        assertThatThrownBy(() -> palletService.getPalletById(palletId))
            .isInstanceOf(PalletNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw BusinessException when updating disabled pallet")
    void shouldThrowBusinessExceptionWhenUpdatingDisabledPallet() {
        // Given: Disabled pallet exists
        Long palletId = 1L;
//        Pallet disabledPallet = PalletTestFixtures.createDisabledPallet();
//        when(palletRepository.findById(palletId)).thenReturn(Optional.of(disabledPallet));

        // When/Then: Should throw BusinessException
        assertThatThrownBy(() -> palletService.updatePallet(palletId, null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Pallet is disabled");
    }
//
//    @Test
//    @DisplayName("Should successfully delete pallet by setting enabled to false")
//    void shouldSuccessfullyDeletePallet() {
//        // Given: Pallet exists
//        Long palletId = 1L;
////        Pallet pallet = PalletTestFixtures.createPallet();
//        when(palletRepository.findById(palletId)).thenReturn(Optional.of(pallet));
//        when(palletRepository.save(any(Pallet.class))).thenReturn(pallet);
//
//        // When: Delete pallet
//        GenericResponse response = palletService.deletePallet(palletId);
//
//        // Then: Should set enabled to false
//        verify(palletRepository).save(pallet);
//        assertThat(pallet.isEnabled()).isFalse();
//        assertThat(response.getStatusCode()).isEqualTo(200);
//    }

    @Test
    @DisplayName("Should throw PalletNotFoundException when deleting non-existent pallet")
    void shouldThrowExceptionWhenDeletingNonExistentPallet() {
        // Given: Pallet doesn't exist
        Long palletId = 999L;
        when(palletRepository.findById(palletId)).thenReturn(Optional.empty());

        // When/Then: Should throw PalletNotFoundException
        assertThatThrownBy(() -> palletService.deletePallet(palletId))
            .isInstanceOf(PalletNotFoundException.class);
    }
}
