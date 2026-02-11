package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.order.application.dto.DistanceCalculationRequest;
import com.tupack.palletsortingapi.order.application.dto.DistanceCalculationResponse;
import com.tupack.palletsortingapi.order.application.dto.DistanceDetailDto;
import com.tupack.palletsortingapi.order.application.dto.DurationDetailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for calculating distance and duration between addresses.
 * Prepared for future integration with Google Maps Distance Matrix API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistanceCalculationService {

  /**
   * Calculate distance and duration between two addresses
   * TODO: Integrate with Google Maps Distance Matrix API
   *
   * @param request Distance calculation request
   * @return Distance and duration information
   */
  public DistanceCalculationResponse calculateDistance(DistanceCalculationRequest request) {
    log.info("Calculating distance from '{}' to '{}'",
        request.getOrigin().getAddress(),
        request.getDestination().getAddress());

    String mode = request.getMode() != null ? request.getMode() : "DRIVING";

    // Mock calculation (will be replaced with Google Maps API)
    int distanceMeters = estimateDistanceMeters(request.getOrigin().getAddress(),
        request.getDestination().getAddress());
    int durationSeconds = estimateDurationSeconds(distanceMeters, mode);

    DistanceDetailDto distance = DistanceDetailDto.builder()
        .value(distanceMeters)
        .text(formatDistance(distanceMeters))
        .build();

    DurationDetailDto duration = DurationDetailDto.builder()
        .value(durationSeconds)
        .text(formatDuration(durationSeconds))
        .build();

    String route = estimateRoute(request.getOrigin().getAddress(),
        request.getDestination().getAddress());

    return DistanceCalculationResponse.builder()
        .distance(distance)
        .duration(duration)
        .route(route)
        .build();
  }

  /**
   * Estimate distance in meters (mock implementation)
   * TODO: Replace with Google Maps Distance Matrix API
   */
  private int estimateDistanceMeters(String origin, String destination) {
    // Simple hash-based estimation for demo purposes
    int hash = (origin + destination).hashCode();
    int baseDistance = 10000; // 10 km base
    int variation = Math.abs(hash % 40000); // 0-40 km variation
    return baseDistance + variation;
  }

  /**
   * Estimate duration in seconds based on distance and mode
   */
  private int estimateDurationSeconds(int distanceMeters, String mode) {
    // Average speeds by mode
    double speedKmH = switch (mode.toUpperCase()) {
      case "WALKING" -> 5.0;
      case "BICYCLING" -> 15.0;
      default -> 30.0; // DRIVING with traffic
    };

    double distanceKm = distanceMeters / 1000.0;
    double hours = distanceKm / speedKmH;
    return (int) (hours * 3600);
  }

  /**
   * Format distance for human reading
   */
  private String formatDistance(int meters) {
    if (meters < 1000) {
      return meters + " m";
    } else {
      double km = meters / 1000.0;
      return String.format("%.1f km", km);
    }
  }

  /**
   * Format duration for human reading
   */
  private String formatDuration(int seconds) {
    int hours = seconds / 3600;
    int minutes = (seconds % 3600) / 60;

    if (hours > 0 && minutes > 0) {
      return String.format("%dh %dm", hours, minutes);
    } else if (hours > 0) {
      return String.format("%dh", hours);
    } else {
      return String.format("%dm", minutes);
    }
  }

  /**
   * Estimate route description (mock implementation)
   * TODO: Replace with actual route from Google Maps API
   */
  private String estimateRoute(String origin, String destination) {
    // Mock route description
    return "via Av. Principal";
  }
}
