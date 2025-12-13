package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.order.application.dto.GenericResponse;
import com.tupack.palletsortingapi.order.domain.Warehouse;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.WarehouseRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseService {

  private final WarehouseRepository warehouseRepository;

  @Transactional(readOnly = true)
  public GenericResponse getAllWarehouses() {
    var warehouses = warehouseRepository.findAll();
    return GenericResponse.success(warehouses);
  }

  @Transactional(readOnly = true)
  public GenericResponse getWarehouseById(Long id) {
    var warehouse = warehouseRepository.findById(id)
        .orElseThrow(); // puedes cambiar a una excepción custom si ya manejas 404
    return GenericResponse.success(warehouse);
  }

  public GenericResponse createWarehouse(Warehouse request) {
    // Evitar que el cliente fuerce el ID en un create
    request.setWarehouseId(null);

    Warehouse saved = warehouseRepository.save(request);
    return GenericResponse.success(saved);
  }

  public GenericResponse updateWarehouse(Long id, Warehouse request) {
    return warehouseRepository.findById(id).map(existing -> {
      existing.setName(request.getName());
      existing.setAddress(request.getAddress());
      existing.setPhone(request.getPhone());

      Warehouse updated = warehouseRepository.save(existing);
      return GenericResponse.success(updated);
    }).orElseThrow();
  }

  public GenericResponse deleteWarehouse(Long id) {
    if (!warehouseRepository.existsById(id)) {
      throw new IllegalArgumentException("Warehouse not found");
    }
    warehouseRepository.deleteById(id);
    return GenericResponse.success("Warehouse deleted successfully");
  }
}
