package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.order.application.dto.AnalysisSuggestionDto;
import com.tupack.palletsortingapi.order.application.dto.AnalysisWarningDto;
import com.tupack.palletsortingapi.order.application.dto.OrderAnalysisDto;
import com.tupack.palletsortingapi.order.application.dto.OrderAnalysisRequest;
import com.tupack.palletsortingapi.order.application.dto.OrderAnalysisResponse;
import com.tupack.palletsortingapi.order.domain.Truck;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.TruckRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for analyzing orders and providing optimization suggestions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderAnalysisService {

  private final TruckRepository truckRepository;

  // Constants for analysis
  private static final double MIN_EFFICIENT_UTILIZATION = 0.70; // 70%
  private static final double CONSOLIDATION_THRESHOLD = 0.60; // 60%
  private static final LocalTime PEAK_START = LocalTime.of(7, 0);
  private static final LocalTime PEAK_END = LocalTime.of(9, 30);
  private static final LocalTime AFTERNOON_PEAK_START = LocalTime.of(17, 0);
  private static final LocalTime AFTERNOON_PEAK_END = LocalTime.of(19, 0);

  /**
   * Analyze an order and provide optimization suggestions
   *
   * @param request Order analysis request
   * @return Analysis with suggestions and warnings
   */
  public OrderAnalysisResponse analyzeOrder(OrderAnalysisRequest request) {
    log.info("Analyzing order for pickup date: {}", request.getPickupDate());

    // Calculate total volume and weight
    double totalVolume = calculateTotalVolume(request);
    double totalWeight = calculateTotalWeight(request);

    // Find suitable truck
    Optional<Truck> suitableTruck = findSuitableTruck(totalVolume, totalWeight);

    // Calculate utilization and optimization score
    double utilization = calculateUtilization(totalVolume, totalWeight, suitableTruck);
    int optimizationScore = calculateOptimizationScore(utilization, request.getPickupDate());

    // Build analysis
    OrderAnalysisDto analysis = OrderAnalysisDto.builder()
        .totalVolume(totalVolume)
        .totalWeight(totalWeight)
        .truckUtilization(utilization)
        .optimizationScore(optimizationScore)
        .build();

    // Generate suggestions
    List<AnalysisSuggestionDto> suggestions = generateSuggestions(
        totalVolume, totalWeight, utilization, request.getPickupDate(), suitableTruck);

    // Generate warnings
    List<AnalysisWarningDto> warnings = generateWarnings(
        totalVolume, totalWeight, request.getPickupDate(), suitableTruck);

    return OrderAnalysisResponse.builder()
        .analysis(analysis)
        .suggestions(suggestions)
        .warnings(warnings)
        .build();
  }

  /**
   * Calculate total volume from items
   */
  private double calculateTotalVolume(OrderAnalysisRequest request) {
    return request.getItems().stream()
        .mapToDouble(item -> item.getVolume() * item.getQuantity())
        .sum();
  }

  /**
   * Calculate total weight from items
   */
  private double calculateTotalWeight(OrderAnalysisRequest request) {
    return request.getItems().stream()
        .mapToDouble(item -> item.getWeight() * item.getQuantity())
        .sum();
  }

  /**
   * Find suitable truck for the order
   */
  private Optional<Truck> findSuitableTruck(double volume, double weight) {
    return truckRepository.findOneByVolume(volume, weight, 0.0);
  }

  /**
   * Calculate truck utilization percentage
   */
  private double calculateUtilization(double volume, double weight, Optional<Truck> truck) {
    if (truck.isEmpty()) {
      return 0.0;
    }

    double volumeUtilization = volume / truck.get().getVolume();
    double weightUtilization = weight / truck.get().getWeight();

    // Return the higher utilization (limiting factor)
    return Math.max(volumeUtilization, weightUtilization);
  }

  /**
   * Calculate optimization score (0-100)
   */
  private int calculateOptimizationScore(double utilization, LocalDate pickupDate) {
    int score = 0;

    // Utilization score (max 40 points)
    if (utilization >= 0.9) {
      score += 40; // Excellent utilization
    } else if (utilization >= 0.7) {
      score += 30; // Good utilization
    } else if (utilization >= 0.5) {
      score += 20; // Fair utilization
    } else {
      score += 10; // Poor utilization
    }

    // Timing score (max 30 points)
    long daysUntilPickup = ChronoUnit.DAYS.between(LocalDate.now(), pickupDate);
    if (daysUntilPickup >= 3) {
      score += 30; // Good planning
    } else if (daysUntilPickup >= 2) {
      score += 20; // Adequate planning
    } else {
      score += 10; // Last minute
    }

    // Day of week score (max 30 points)
    DayOfWeek dayOfWeek = pickupDate.getDayOfWeek();
    if (dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.WEDNESDAY
        || dayOfWeek == DayOfWeek.THURSDAY) {
      score += 30; // Best days (mid-week)
    } else if (dayOfWeek == DayOfWeek.MONDAY || dayOfWeek == DayOfWeek.FRIDAY) {
      score += 20; // Good days
    } else {
      score += 0; // Weekend (not ideal for business)
    }

    return score;
  }

  /**
   * Generate optimization suggestions
   */
  private List<AnalysisSuggestionDto> generateSuggestions(double volume, double weight,
      double utilization, LocalDate pickupDate, Optional<Truck> truck) {
    List<AnalysisSuggestionDto> suggestions = new ArrayList<>();

    // Consolidation suggestion
    if (utilization < CONSOLIDATION_THRESHOLD && truck.isPresent()) {
      double remainingVolume = truck.get().getVolume() - volume;
      BigDecimal potentialSavings = BigDecimal.valueOf(remainingVolume * 50)
          .setScale(2, RoundingMode.HALF_UP);

      suggestions.add(AnalysisSuggestionDto.builder()
          .type("CONSOLIDATION")
          .message(String.format("Podrías agregar %.1f m³ más sin costo adicional", remainingVolume))
          .potentialSavings(potentialSavings)
          .build());
    }

    // Timing suggestion
    long daysUntilPickup = ChronoUnit.DAYS.between(LocalDate.now(), pickupDate);
    if (daysUntilPickup < 2) {
      LocalDate suggestedDate = LocalDate.now().plusDays(3);
      suggestions.add(AnalysisSuggestionDto.builder()
          .type("TIMING")
          .message("Cambiar a fecha de recojo +2 días reduce costo en 10%")
          .alternativeDate(suggestedDate)
          .potentialSavings(BigDecimal.valueOf(125))
          .build());
    }

    // Weekend suggestion
    if (pickupDate.getDayOfWeek() == DayOfWeek.SATURDAY
        || pickupDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
      LocalDate nextMonday = pickupDate.with(DayOfWeek.MONDAY);
      if (nextMonday.isBefore(pickupDate)) {
        nextMonday = nextMonday.plusWeeks(1);
      }

      suggestions.add(AnalysisSuggestionDto.builder()
          .type("TIMING")
          .message("Programar para día de semana puede mejorar disponibilidad y costos")
          .alternativeDate(nextMonday)
          .build());
    }

    return suggestions;
  }

  /**
   * Generate warnings
   */
  private List<AnalysisWarningDto> generateWarnings(double volume, double weight,
      LocalDate pickupDate, Optional<Truck> truck) {
    List<AnalysisWarningDto> warnings = new ArrayList<>();

    // Capacity warning
    if (truck.isEmpty()) {
      warnings.add(AnalysisWarningDto.builder()
          .type("CAPACITY_EXCEEDED")
          .message("No hay camiones disponibles que cumplan con los requisitos de capacidad")
          .impact("Es posible que necesites dividir el pedido en múltiples envíos")
          .build());
    }

    // Peak hour warning (assuming early morning pickup)
    LocalTime assumedPickupTime = LocalTime.of(8, 0); // Default assumption
    if (isWithinPeakHours(assumedPickupTime)) {
      warnings.add(AnalysisWarningDto.builder()
          .type("PEAK_HOUR")
          .message("Hora de recojo coincide con tráfico pesado")
          .impact("Posible retraso de 30-45 minutos")
          .build());
    }

    // Last minute warning
    long daysUntilPickup = ChronoUnit.DAYS.between(LocalDate.now(), pickupDate);
    if (daysUntilPickup < 1) {
      warnings.add(AnalysisWarningDto.builder()
          .type("URGENCY")
          .message("Pedido con menos de 24 horas de anticipación")
          .impact("Tarifa de urgencia de 20% aplicable, disponibilidad limitada")
          .build());
    }

    return warnings;
  }

  /**
   * Check if time is within peak hours
   */
  private boolean isWithinPeakHours(LocalTime time) {
    return (time.isAfter(PEAK_START) && time.isBefore(PEAK_END))
        || (time.isAfter(AFTERNOON_PEAK_START) && time.isBefore(AFTERNOON_PEAK_END));
  }
}
