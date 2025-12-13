package com.tupack.palletsortingapi.order.application;

import com.tupack.palletsortingapi.order.application.dto.GenericResponse;
import com.tupack.palletsortingapi.order.application.dto.TruckDto;
import com.tupack.palletsortingapi.order.application.mapper.TruckMapper;
import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.infrastructure.outbound.dabatase.TruckRepository;
import org.springframework.stereotype.Service;

@Service
public class TruckService {

  private final TruckRepository truckRepository;
  private final TruckMapper truckMapper;

  public TruckService(TruckRepository truckRepository, TruckMapper truckMapper) {
    this.truckRepository = truckRepository;
    this.truckMapper = truckMapper;
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
    Truck truck = truckMapper.toEntity(truckDTO);
    truck.setEnabled(true);
    Truck saved = truckRepository.save(truck);
    return GenericResponse.success(truckMapper.toDto(saved));
  }

  public GenericResponse updateTruck(Long id, TruckDto truckDTO) {
    return truckRepository.findById(id).map(truck -> {
      truckMapper.updateEntity(truckDTO, truck);
      Truck updated = truckRepository.save(truck);
      return GenericResponse.success(truckMapper.toDto(updated));
    }).orElseThrow();
  }

  public GenericResponse deleteTruck(Long id) {
    return truckRepository.findById(id).map(truck -> {
      truck.setEnabled(false);
      truckRepository.save(truck);
      return GenericResponse.success("Truck deleted successfully");
    }).orElseThrow();
  }
}