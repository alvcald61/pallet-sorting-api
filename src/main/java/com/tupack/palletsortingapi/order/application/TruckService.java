package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.TruckDto;
import com.tupack.palletsortingapi.order.application.mapper.TruckMapper;
import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.TruckRepository;
import com.tupack.palletsortingapi.user.domain.Driver;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.DriverRepository;
import org.springframework.stereotype.Service;

@Service
public class TruckService {

  private final TruckRepository truckRepository;
  private final TruckMapper truckMapper;
  private final DriverRepository driverRepository;

  public TruckService(TruckRepository truckRepository, TruckMapper truckMapper,
      DriverRepository driverRepository) {
    this.truckRepository = truckRepository;
    this.truckMapper = truckMapper;
    this.driverRepository = driverRepository;
  }

  public GenericResponse getAllTrucks() {
    var trucks = truckRepository.findAllByEnabled(true).stream().map(truckMapper::toDto).toList();

    return GenericResponse.success(trucks);
  }

  public GenericResponse getTruckById(Long id) {
    var truck = truckRepository.findById(id).map(truckMapper::toDto);
    return truck.map(GenericResponse::success)
        .orElseThrow();
  }

  public GenericResponse createTruck(TruckDto truckDTO) {
    Driver driver = driverRepository.findById(truckDTO.getDriverId()).orElseThrow();
    Truck truck = truckMapper.toEntity(truckDTO);
    truck.setDriver(driver);
    truck.setEnabled(true);
    Truck saved = truckRepository.save(truck);
    return GenericResponse.success(truckMapper.toDto(saved));
  }

  public GenericResponse updateTruck(Long id, TruckDto truckDTO) {
    return truckRepository.findById(id).map(truck -> {
      truckMapper.updateEntity(truckDTO, truck);
      updateDriver(truck, truckDTO.getDriverId());
      Truck updated = truckRepository.save(truck);
      return GenericResponse.success(truckMapper.toDto(updated));
    }).orElseThrow();
  }

  private void updateDriver(Truck truck, Long driverId) {
    if(truck.getDriver() == null || !truck.getDriver().getDriverId().equals(driverId)) {
      Driver driver = driverRepository.findById(driverId).orElseThrow();
      truck.setDriver(driver);
    }
  }

  public GenericResponse deleteTruck(Long id) {
    return truckRepository.findById(id).map(truck -> {
      truck.setEnabled(false);
      truckRepository.save(truck);
      return GenericResponse.success("Truck deleted successfully");
    }).orElseThrow();
  }
}
