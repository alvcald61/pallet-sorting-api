package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.domain.Price;
import com.tupack.palletsortingapi.order.domain.PriceCondition;
import com.tupack.palletsortingapi.order.domain.Zone;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceConditionRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PriceRepository;
import com.tupack.palletsortingapi.user.domain.Client;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for calculating order pricing based on volume, weight, and destination.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPricingService {

  private final ZoneResolverService zoneResolverService;
  private final PriceConditionRepository priceConditionRepository;
  private final PriceRepository priceRepository;

  /**
   * Calculate order amount based on request data and zone
   *
   * @param request Packing request with volume and weight
   * @param zone    Destination zone
   * @return Calculated price or null if not applicable
   */
  public BigDecimal calculateOrderAmount(SolvePackingRequest request, Zone zone, Client client) {
    // Special logic for Lima - puede extenderse a otras ciudades
    if (isLimaDelivery(request)) {
      return calculateLimaPrice(request, client);
    }

    // Lógica general para otras zonas (puede implementarse después)
    return calculateGeneralPrice(request, zone);
  }

  /**
   * Calculate price for Lima deliveries
   */
  private BigDecimal calculateLimaPrice(SolvePackingRequest request, Client client) {
    Zone requestZone = zoneResolverService.resolveZoneByDistrict(
        request.getToAddress().district());

    PriceCondition matchCondition = priceConditionRepository
        .findByVolumeAndWeight(request.getTotalVolume(), request.getTotalWeight())
        .orElseThrow(() -> new BusinessException(
            String.format("No price condition found for volume: %.2f and weight: %.2f",
                request.getTotalVolume(), request.getTotalWeight()),
            "PRICE_CONDITION_NOT_FOUND"));

    Price price = priceRepository.findByZoneAndPriceConditionAndClient(requestZone, matchCondition, client);

    if (price == null) {
      throw new BusinessException(
          String.format("No price found for zone: %s and condition: volume=%.2f, weight=%.2f",
              requestZone.getDistrict(), request.getTotalVolume(), request.getTotalWeight()),
          "PRICE_NOT_FOUND");
    }

    return price.getPrice();
  }

  /**
   * Calculate price for general deliveries (outside Lima)
   * Can be extended with different pricing strategies
   */
  private BigDecimal calculateGeneralPrice(SolvePackingRequest request, Zone zone) {
    // TODO: Implementar lógica de pricing para provincias
    // Por ahora retorna null (precio se calculará manualmente en PRE_APPROVED)
    return null;
  }

  /**
   * Check if delivery is to Lima
   */
  private boolean isLimaDelivery(SolvePackingRequest request) {
    return request.getToAddress().city().equalsIgnoreCase("lima");
  }

  /**
   * Validate if pricing can be calculated automatically for the client
   *
   * @param isTrustClient Whether the client is a trusted client
   * @return true if automatic pricing should be applied
   */
  public boolean shouldCalculatePrice(boolean isTrustClient) {
    return isTrustClient;
  }
}
