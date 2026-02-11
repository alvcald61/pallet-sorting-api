package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.order.application.dto.AddressValidationRequest;
import com.tupack.palletsortingapi.order.application.dto.AddressValidationResponse;
import com.tupack.palletsortingapi.order.application.dto.CoordinatesDto;
import com.tupack.palletsortingapi.order.application.dto.NormalizedAddressDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for validating and normalizing addresses.
 * Prepared for future integration with Google Maps Geocoding API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AddressValidationService {

  /**
   * Validate and normalize an address
   * TODO: Integrate with Google Maps Geocoding API
   *
   * @param request Address validation request
   * @return Validation result with normalized address and coordinates
   */
  public AddressValidationResponse validateAddress(AddressValidationRequest request) {
    log.info("Validating address: {}, {}, {}", request.getAddress(), request.getDistrict(), request.getCity());

    // Basic validation logic (will be replaced with Google Maps API)
    boolean isValid = isAddressValid(request);
    String confidence = calculateConfidence(request);

    // Normalize address (currently just capitalizes properly)
    NormalizedAddressDto normalized = normalizeAddress(request);

    // Mock coordinates (will be replaced with actual geocoding)
    CoordinatesDto coordinates = generateMockCoordinates(request);

    // Mock place ID (will come from Google Maps API)
    String placeId = generateMockPlaceId(request);

    return AddressValidationResponse.builder()
        .isValid(isValid)
        .normalized(normalized)
        .coordinates(coordinates)
        .placeId(placeId)
        .confidence(confidence)
        .build();
  }

  /**
   * Basic address validation
   * Checks if required fields are not empty and meet basic criteria
   */
  private boolean isAddressValid(AddressValidationRequest request) {
    // Basic validation rules
    if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
      return false;
    }
    if (request.getDistrict() == null || request.getDistrict().trim().isEmpty()) {
      return false;
    }
    if (request.getCity() == null || request.getCity().trim().isEmpty()) {
      return false;
    }
    if (request.getState() == null || request.getState().trim().isEmpty()) {
      return false;
    }

    // Check minimum address length
    if (request.getAddress().length() < 5) {
      return false;
    }

    return true;
  }

  /**
   * Normalize address by capitalizing and formatting properly
   */
  private NormalizedAddressDto normalizeAddress(AddressValidationRequest request) {
    return NormalizedAddressDto.builder()
        .address(capitalizeWords(request.getAddress()))
        .district(capitalizeWords(request.getDistrict()))
        .city(capitalizeWords(request.getCity()))
        .state(capitalizeWords(request.getState()))
        .country("Perú")
        .build();
  }

  /**
   * Calculate confidence level based on address completeness
   */
  private String calculateConfidence(AddressValidationRequest request) {
    int score = 0;

    // Check address has street number
    if (request.getAddress().matches(".*\\d+.*")) {
      score += 30;
    }

    // Check address length (more specific = higher confidence)
    if (request.getAddress().length() > 20) {
      score += 30;
    }

    // Check known city
    if (isKnownCity(request.getCity())) {
      score += 40;
    }

    if (score >= 70) {
      return "HIGH";
    } else if (score >= 40) {
      return "MEDIUM";
    } else {
      return "LOW";
    }
  }

  /**
   * Generate mock coordinates for development
   * TODO: Replace with Google Maps Geocoding API
   */
  private CoordinatesDto generateMockCoordinates(AddressValidationRequest request) {
    // Mock coordinates for Lima, Peru
    // In production, these will come from Google Maps API
    double baseLat = -12.0464;
    double baseLng = -77.0428;

    // Add small random offset based on district hash (for demo purposes)
    int hash = request.getDistrict().hashCode();
    double latOffset = (hash % 100) / 1000.0;
    double lngOffset = (hash % 100) / 1000.0;

    return CoordinatesDto.builder()
        .latitude(baseLat + latOffset)
        .longitude(baseLng + lngOffset)
        .build();
  }

  /**
   * Generate mock place ID
   * TODO: Replace with actual Google Maps Place ID
   */
  private String generateMockPlaceId(AddressValidationRequest request) {
    return "ChIJ" + Integer.toHexString(request.getAddress().hashCode());
  }

  /**
   * Check if city is known in our system
   */
  private boolean isKnownCity(String city) {
    String[] knownCities = {"Lima", "Callao", "Arequipa", "Trujillo", "Cusco"};
    for (String knownCity : knownCities) {
      if (knownCity.equalsIgnoreCase(city.trim())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Capitalize first letter of each word
   */
  private String capitalizeWords(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }

    String[] words = text.trim().toLowerCase().split("\\s+");
    StringBuilder result = new StringBuilder();

    for (String word : words) {
      if (word.length() > 0) {
        result.append(Character.toUpperCase(word.charAt(0)));
        if (word.length() > 1) {
          result.append(word.substring(1));
        }
        result.append(" ");
      }
    }

    return result.toString().trim();
  }
}
