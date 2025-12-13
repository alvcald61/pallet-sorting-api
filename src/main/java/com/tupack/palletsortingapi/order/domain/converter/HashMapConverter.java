package com.tupack.palletsortingapi.order.domain.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HashMapConverter implements AttributeConverter<Map<String, Object>, String> {

  @Override
  public String convertToDatabaseColumn(Map<String, Object> customerInfo) {

    String customerInfoJson = null;
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      customerInfoJson = objectMapper.writeValueAsString(customerInfo);
    } catch (final JsonProcessingException e) {
      log.error("JSON writing error", e);
    }

    return customerInfoJson;
  }

  @Override
  public Map<String, Object> convertToEntityAttribute(String customerInfoJSON) {

    Map<String, Object> customerInfo = null;
    ObjectMapper objectMapper = new ObjectMapper();

    try {
      customerInfo = objectMapper.readValue(customerInfoJSON,
          new TypeReference<HashMap<String, Object>>() {});
    } catch (final IOException e) {
      log.error("JSON reading error", e);
    }

    return customerInfo;
  }
}