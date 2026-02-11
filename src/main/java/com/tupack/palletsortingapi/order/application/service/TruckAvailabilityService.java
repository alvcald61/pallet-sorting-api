package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.order.application.dto.AvailabilityRecommendationDto;
import com.tupack.palletsortingapi.order.application.dto.AvailableTruckDto;
import com.tupack.palletsortingapi.order.application.dto.TruckAvailabilityResponse;
import com.tupack.palletsortingapi.order.application.dto.TruckCapacityDto;
import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.TruckRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for checking truck availability for specific dates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TruckAvailabilityService {

  private final TruckRepository truckRepository;
  private final OrderRepository orderRepository;

  private static final String[] TIME_SLOTS = {
      "08:00", "09:00", "10:00", "11:00", "12:00",
      "13:00", "14:00", "15:00", "16:00", "17:00"
  };

  /**
   * Check truck availability for a specific date and requirements
   *
   * @param date            Pickup date (YYYY-MM-DD)
   * @param requiredVolume  Required volume in m³
   * @param requiredWeight  Required weight in kg
   * @return Availability information with available trucks and recommendations
   */
  public TruckAvailabilityResponse checkAvailability(String date, Double requiredVolume,
      Double requiredWeight) {
    log.info("Checking truck availability for date: {}, volume: {}, weight: {}",
        date, requiredVolume, requiredWeight);

    LocalDate pickupDate = LocalDate.parse(date);

    // Get all enabled trucks
    List<Truck> allTrucks = truckRepository.findAllByEnabled(true);

    // Filter trucks that meet capacity requirements
    List<Truck> suitableTrucks = allTrucks.stream()
        .filter(truck -> truck.getVolume() >= requiredVolume
            && truck.getWeight() >= requiredWeight)
        .collect(Collectors.toList());

    // Check availability for each suitable truck
    List<AvailableTruckDto> availableTrucks = new ArrayList<>();
    for (Truck truck : suitableTrucks) {
      List<String> availableSlots = getAvailableTimeSlots(truck, pickupDate);
      if (!availableSlots.isEmpty()) {
        availableTrucks.add(toAvailableTruckDto(truck, availableSlots));
      }
    }

    // Generate recommendations
    AvailabilityRecommendationDto recommendation = generateRecommendation(availableTrucks,
        pickupDate);

    return TruckAvailabilityResponse.builder()
        .available(!availableTrucks.isEmpty())
        .trucks(availableTrucks)
        .recommendations(recommendation)
        .build();
  }

  /**
   * Get available time slots for a truck on a specific date
   */
  private List<String> getAvailableTimeSlots(Truck truck, LocalDate date) {
    List<String> availableSlots = new ArrayList<>();

    for (String timeSlot : TIME_SLOTS) {
      LocalTime time = LocalTime.parse(timeSlot, DateTimeFormatter.ofPattern("HH:mm"));
      LocalDateTime startTime = LocalDateTime.of(date, time);
      LocalDateTime endTime = startTime.plusHours(4); // Assume 4-hour delivery window

      // Check if truck is available during this time slot
      boolean isAvailable = !orderRepository.existsOverlappingOrder(startTime, endTime, truck);

      if (isAvailable) {
        availableSlots.add(timeSlot);
      }
    }

    return availableSlots;
  }

  /**
   * Convert Truck entity to AvailableTruckDto
   */
  private AvailableTruckDto toAvailableTruckDto(Truck truck, List<String> availableSlots) {
    return AvailableTruckDto.builder()
        .truckId("truck-" + truck.getId())
        .licensePlate(truck.getLicensePlate())
        .capacity(TruckCapacityDto.builder()
            .volume(truck.getVolume())
            .weight(truck.getWeight())
            .build())
        .availableSlots(availableSlots)
        .build();
  }

  /**
   * Generate availability recommendations
   */
  private AvailabilityRecommendationDto generateRecommendation(
      List<AvailableTruckDto> availableTrucks, LocalDate pickupDate) {

    if (availableTrucks.isEmpty()) {
      return AvailabilityRecommendationDto.builder()
          .preferredTimeSlot("N/A")
          .reason("No hay camiones disponibles para esta fecha")
          .build();
    }

    // Find the time slot with most available trucks
    String mostAvailableSlot = findMostAvailableTimeSlot(availableTrucks);
    String reason = determineRecommendationReason(mostAvailableSlot, pickupDate);

    return AvailabilityRecommendationDto.builder()
        .preferredTimeSlot(mostAvailableSlot)
        .reason(reason)
        .build();
  }

  /**
   * Find the time slot with most truck availability
   */
  private String findMostAvailableTimeSlot(List<AvailableTruckDto> trucks) {
    // Count how many trucks are available per time slot
    int maxCount = 0;
    String bestSlot = "08:00";

    for (String slot : TIME_SLOTS) {
      int count = 0;
      for (AvailableTruckDto truck : trucks) {
        if (truck.getAvailableSlots().contains(slot)) {
          count++;
        }
      }
      if (count > maxCount) {
        maxCount = count;
        bestSlot = slot;
      }
    }

    return bestSlot;
  }

  /**
   * Determine recommendation reason based on time slot and date
   */
  private String determineRecommendationReason(String timeSlot, LocalDate pickupDate) {
    LocalTime time = LocalTime.parse(timeSlot, DateTimeFormatter.ofPattern("HH:mm"));

    // Morning slots (before 11:00)
    if (time.isBefore(LocalTime.of(11, 0))) {
      return "Menos tráfico, ruta más rápida";
    }

    // Afternoon slots (after 14:00)
    if (time.isAfter(LocalTime.of(14, 0))) {
      return "Mayor disponibilidad de camiones";
    }

    // Midday slots
    return "Horario estándar, buena disponibilidad";
  }
}
