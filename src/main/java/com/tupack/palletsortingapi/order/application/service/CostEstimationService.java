package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.order.application.dto.CostBreakdownDto;
import com.tupack.palletsortingapi.order.application.dto.DistanceInfoDto;
import com.tupack.palletsortingapi.order.application.dto.EstimateCostRequest;
import com.tupack.palletsortingapi.order.application.dto.EstimateCostResponse;
import com.tupack.palletsortingapi.order.domain.Price;
import com.tupack.palletsortingapi.order.domain.PriceCondition;
import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceConditionRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceRepository;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for estimating order costs before actual order creation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CostEstimationService {

  private final ZoneResolverService zoneResolverService;
  private final PriceConditionRepository priceConditionRepository;
  private final PriceRepository priceRepository;
  private final ClientRepository clientRepository;

  private static final BigDecimal VOLUME_RATE = BigDecimal.valueOf(50); // $50 per m³
  private static final BigDecimal WEIGHT_RATE = BigDecimal.valueOf(0.10); // $0.10 per kg
  private static final BigDecimal URGENCY_MULTIPLIER = BigDecimal.valueOf(1.20); // 20% extra
  private static final long URGENCY_THRESHOLD_HOURS = 48;

  /**
   * Calculate estimated cost for an order
   *
   * @param request Estimation request with items and addresses
   * @return Estimated cost with breakdown
   */
  public EstimateCostResponse estimateCost(EstimateCostRequest request) {
    log.info("Estimating cost for order type: {}", request.getOrderType());

    // Resolve zone from destination
    Zone zone = zoneResolverService.resolveZone(request.getToAddress());

    // Calculate base cost using existing pricing logic
    BigDecimal baseCost = calculateBaseCost(request, zone);

    // Calculate individual cost components
    BigDecimal volumeCost = calculateVolumeCost(request.getTotalVolume());
    BigDecimal weightCost = calculateWeightCost(request.getTotalWeight());
    BigDecimal distanceCost = calculateDistanceCost(zone);

    // Calculate urgency fee if pickup date is within 48 hours
    BigDecimal urgencyFee = calculateUrgencyFee(request.getPickupDate(), baseCost);

    // Calculate total estimated cost
    BigDecimal estimatedCost = baseCost.add(urgencyFee);

    // Build breakdown
    CostBreakdownDto breakdown = CostBreakdownDto.builder()
        .baseCost(baseCost)
        .volumeCost(volumeCost)
        .weightCost(weightCost)
        .distanceCost(distanceCost)
        .urgencyFee(urgencyFee)
        .build();

    // Build distance info (mock data for now, will be replaced with Google Maps API)
    DistanceInfoDto distanceInfo = DistanceInfoDto.builder()
        .kilometers(estimateDistance(request.getFromAddress(), request.getToAddress()))
        .estimatedDuration(estimateDuration(zone))
        .build();

    return EstimateCostResponse.builder()
        .estimatedCost(estimatedCost)
        .breakdown(breakdown)
        .distance(distanceInfo)
        .currency("USD")
        .build();
  }

  /**
   * Calculate base cost using existing pricing logic (zone + price condition)
   */
  private BigDecimal calculateBaseCost(EstimateCostRequest request, Zone zone) {
    // For Lima deliveries, use existing pricing table
    if (isLimaDelivery(request) && request.getClientId() != null) {
      Client client = clientRepository.findById(request.getClientId()).orElse(null);
      if (client != null) {
        return calculateLimaPrice(request, zone, client);
      }
    }

    // For other zones, use formula-based calculation
    BigDecimal volumeCost = calculateVolumeCost(request.getTotalVolume());
    BigDecimal weightCost = calculateWeightCost(request.getTotalWeight());
    BigDecimal distanceCost = calculateDistanceCost(zone);

    return volumeCost.add(weightCost).add(distanceCost);
  }

  /**
   * Calculate price for Lima deliveries using price table
   */
  private BigDecimal calculateLimaPrice(EstimateCostRequest request, Zone zone, Client client) {
    try {
      PriceCondition matchCondition = priceConditionRepository
          .findByVolumeAndWeight(request.getTotalVolume(), request.getTotalWeight())
          .orElseThrow(() -> new BusinessException(
              String.format("No price condition found for volume: %.2f and weight: %.2f",
                  request.getTotalVolume(), request.getTotalWeight()),
              "PRICE_CONDITION_NOT_FOUND"));

      Price price = priceRepository.findByZoneAndPriceConditionAndClient(zone, matchCondition, client);

      if (price == null) {
        // If no exact price found, use formula-based calculation
        log.warn("No price found for zone: {}, using formula-based calculation", zone.getDistrict());
        return calculateFormulaBasedPrice(request);
      }

      return price.getPrice();
    } catch (BusinessException e) {
      // If no price condition matches, use formula-based calculation
      log.warn("Price calculation failed, using formula-based calculation: {}", e.getMessage());
      return calculateFormulaBasedPrice(request);
    }
  }

  /**
   * Calculate price using formula when no price table entry exists
   */
  private BigDecimal calculateFormulaBasedPrice(EstimateCostRequest request) {
    BigDecimal volumeCost = calculateVolumeCost(request.getTotalVolume());
    BigDecimal weightCost = calculateWeightCost(request.getTotalWeight());
    return volumeCost.add(weightCost);
  }

  /**
   * Calculate cost based on volume
   * Formula: volume (m³) * $50
   */
  private BigDecimal calculateVolumeCost(Double volume) {
    return BigDecimal.valueOf(volume)
        .multiply(VOLUME_RATE)
        .setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Calculate cost based on weight
   * Formula: (weight kg / 100) * $10
   */
  private BigDecimal calculateWeightCost(Double weight) {
    return BigDecimal.valueOf(weight)
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        .multiply(BigDecimal.TEN)
        .setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Calculate cost based on distance/zone
   * For now, use zone's max delivery time as a proxy
   */
  private BigDecimal calculateDistanceCost(Zone zone) {
    // Approximate cost based on delivery time
    // $5 per hour of delivery time
    long deliveryHours = zone.getMaxDeliveryTime() / 60;
    return BigDecimal.valueOf(deliveryHours)
        .multiply(BigDecimal.valueOf(5))
        .setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Calculate urgency fee if pickup is within 48 hours
   */
  private BigDecimal calculateUrgencyFee(LocalDate pickupDate, BigDecimal baseCost) {
    LocalDate today = LocalDate.now();
    long hoursUntilPickup = ChronoUnit.HOURS.between(today.atStartOfDay(), pickupDate.atStartOfDay());

    if (hoursUntilPickup < URGENCY_THRESHOLD_HOURS) {
      // Add 20% urgency fee
      return baseCost.multiply(URGENCY_MULTIPLIER)
          .subtract(baseCost)
          .setScale(2, RoundingMode.HALF_UP);
    }

    return BigDecimal.ZERO;
  }

  /**
   * Check if delivery is to Lima
   */
  private boolean isLimaDelivery(EstimateCostRequest request) {
    return request.getToAddress().city().equalsIgnoreCase("lima");
  }

  /**
   * Estimate distance between addresses
   * TODO: Replace with Google Maps Distance Matrix API
   */
  private Double estimateDistance(com.tupack.palletsortingapi.order.application.dto.AddressDto from,
      com.tupack.palletsortingapi.order.application.dto.AddressDto to) {
    // Mock calculation - will be replaced with actual Google Maps API call
    // For now, return a reasonable estimate based on districts
    if (from.district().equalsIgnoreCase(to.district())) {
      return 5.0; // Same district ~ 5km
    } else if (from.city().equalsIgnoreCase(to.city())) {
      return 25.0; // Same city ~ 25km
    } else {
      return 50.0; // Different city ~ 50km
    }
  }

  /**
   * Estimate duration based on zone
   */
  private String estimateDuration(Zone zone) {
    long minutes = zone.getMaxDeliveryTime();
    long hours = minutes / 60;
    long mins = minutes % 60;

    if (hours > 0 && mins > 0) {
      return String.format("%dh %dm", hours, mins);
    } else if (hours > 0) {
      return String.format("%dh", hours);
    } else {
      return String.format("%dm", mins);
    }
  }
}
