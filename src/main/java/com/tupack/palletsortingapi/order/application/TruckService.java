package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.DriverNotFoundException;
import com.tupack.palletsortingapi.common.exception.TruckNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.TruckDto;
import com.tupack.palletsortingapi.order.application.mapper.TruckMapper;
import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.TruckRepository;
import com.tupack.palletsortingapi.user.domain.Driver;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TruckService {

  private final TruckRepository truckRepository;
  private final TruckMapper truckMapper;
  private final DriverRepository driverRepository;

  @Transactional(readOnly = true)
  public GenericResponse getAllTrucks() {
    var trucks = truckRepository.findAllByEnabled(true).stream().map(truckMapper::toDto).toList();

    return GenericResponse.success(trucks);
  }

  @Transactional(readOnly = true)
  public GenericResponse getTruckById(Long id) {
    var truck = truckRepository.findById(id).map(truckMapper::toDto);
    return truck.map(GenericResponse::success)
        .orElseThrow(() -> new TruckNotFoundException(id));
  }

  @Transactional
  public GenericResponse createTruck(TruckDto truckDTO) {
    Driver driver = driverRepository.findById(truckDTO.getDriverId())
        .orElseThrow(() -> new DriverNotFoundException(truckDTO.getDriverId()));
    Truck truck = truckMapper.toEntity(truckDTO);
    truck.setDriver(driver);
    truck.setEnabled(true);
    Truck saved = truckRepository.save(truck);
    driver.setTruck(saved);
    driverRepository.save(driver);
    return GenericResponse.success(truckMapper.toDto(saved));
  }

  @Transactional
  public GenericResponse updateTruck(Long id, TruckDto truckDTO) {
    return truckRepository.findById(id).map(truck -> {
      truckMapper.updateEntity(truckDTO, truck);
      updateDriver(truck, truckDTO.getDriverId());
      Truck updated = truckRepository.save(truck);
      return GenericResponse.success(truckMapper.toDto(updated));
    }).orElseThrow(() -> new TruckNotFoundException(id));
  }

  private void updateDriver(Truck truck, Long driverId) {
    if(truck.getDriver() == null || !truck.getDriver().getDriverId().equals(driverId)) {
      Driver driver = driverRepository.findById(driverId)
          .orElseThrow(() -> new DriverNotFoundException(driverId));
      truck.setDriver(driver);
    }
  }

  @Transactional
  public GenericResponse deleteTruck(Long id) {
    return truckRepository.findById(id).map(truck -> {
      truck.setEnabled(false);
      truckRepository.save(truck);
      return GenericResponse.success("Truck deleted successfully");
    }).orElseThrow(() -> new TruckNotFoundException(id));
  }
}
