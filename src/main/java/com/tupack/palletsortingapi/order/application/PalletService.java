package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.common.exception.PalletNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.CreatePalletRequest;
import com.tupack.palletsortingapi.order.application.dto.PalletDto;
import com.tupack.palletsortingapi.order.application.mapper.PalletMapper;
import com.tupack.palletsortingapi.order.domain.Pallet;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PalletRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PalletService {

  private final PalletRepository palletRepository;
  private final PalletMapper palletMapper;

  /**
   * Get all active pallets
   */
  public GenericResponse getAllActivePallets() {
    List<PalletDto> palletDtos =
        palletRepository.findAllByEnabled(true).stream().map(palletMapper::toDto)
        .collect(Collectors.toList());
    log.info("Retrieved {} active pallets", palletDtos.size());
    return GenericResponse.success(palletDtos);
  }

  /**
   * Get a pallet by ID
   */
  public GenericResponse getPalletById(Long id) {
    var pallet = palletRepository.findById(id)
        .filter(Pallet::isEnabled)
        .map(palletMapper::toDto);
    log.info("Retrieved pallet with id: {}", id);
    return pallet.map(GenericResponse::success)
        .orElseThrow(() -> new PalletNotFoundException(id));
  }

  /**
   * Create a new pallet
   */
  @Transactional
  public GenericResponse createPallet(CreatePalletRequest request) {
    Pallet pallet = palletMapper.toEntity(request);
    pallet.setEnabled(true);
    Pallet saved = palletRepository.save(pallet);
    log.info("Successfully created pallet with id: {}", saved.getId());
    return GenericResponse.success(palletMapper.toDto(saved));
  }

  /**
   * Update an existing pallet
   */
  @Transactional
  public GenericResponse updatePallet(Long id, CreatePalletRequest request) {
    return palletRepository.findById(id)
        .map(pallet -> {
          if (!pallet.isEnabled()) {
            log.warn("Attempt to update disabled pallet with id: {}", id);
            throw new BusinessException("Pallet is disabled", "PALLET_DISABLED");
          }

          palletMapper.updateEntity(request, pallet);
          Pallet updated = palletRepository.save(pallet);
          log.info("Successfully updated pallet with id: {}", id);
          return GenericResponse.success(palletMapper.toDto(updated));
        })
        .orElseThrow(() -> new PalletNotFoundException(id));
  }

  /**
   * Delete (soft delete) a pallet
   */
  @Transactional
  public GenericResponse deletePallet(Long id) {
    return palletRepository.findById(id)
        .map(pallet -> {
          pallet.setEnabled(false);
          palletRepository.save(pallet);
          log.info("Successfully deleted (soft) pallet with id: {}", id);
          return GenericResponse.success("Pallet eliminado exitosamente");
        })
        .orElseThrow(() -> new PalletNotFoundException(id));
  }
}
