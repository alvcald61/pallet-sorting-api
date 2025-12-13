package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.order.application.dto.CreatePalletRequest;
import com.tupack.palletsortingapi.order.application.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.PalletDto;
import com.tupack.palletsortingapi.order.application.mapper.PalletMapper;
import com.tupack.palletsortingapi.order.domain.Pallet;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.PalletRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
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
    return GenericResponse.success(palletDtos);
  }

  /**
   * Get a pallet by ID
   */
  public GenericResponse getPalletById(Long id) {
    var pallet = palletRepository.findById(id)
        .filter(Pallet::isEnabled)
        .map(palletMapper::toDto);
    return pallet.map(GenericResponse::success)
        .orElseGet(() -> GenericResponse.error("Pallet no encontrado"));
  }

  /**
   * Create a new pallet
   */
  @Transactional
  public GenericResponse createPallet(CreatePalletRequest request) {
    try {
      Pallet pallet = palletMapper.toEntity(request);
      pallet.setEnabled(true);
      Pallet saved = palletRepository.save(pallet);
      return GenericResponse.success(palletMapper.toDto(saved));
    } catch (Exception e) {
      return GenericResponse.error("Error al crear el pallet: " + e.getMessage());
    }
  }

  /**
   * Update an existing pallet
   */
  @Transactional
  public GenericResponse updatePallet(Long id, CreatePalletRequest request) {
    return palletRepository.findById(id)
        .map(pallet -> {
          if (!pallet.isEnabled()) {
            return GenericResponse.error("Pallet desactivado");
          }

          palletMapper.updateEntity(request, pallet);
          Pallet updated = palletRepository.save(pallet);
          return GenericResponse.success(palletMapper.toDto(updated));
        })
        .orElse(GenericResponse.error("Pallet no encontrado"));
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
          return GenericResponse.success("Pallet eliminado exitosamente");
        })
        .orElse(GenericResponse.error("Pallet no encontrado"));
  }
}
