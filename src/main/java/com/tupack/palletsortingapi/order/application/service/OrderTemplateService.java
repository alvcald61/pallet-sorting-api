package com.tupack.palletsortingapi.order.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.common.exception.ClientNotFoundException;
import com.tupack.palletsortingapi.order.application.dto.CreateOrderTemplateRequest;
import com.tupack.palletsortingapi.order.application.dto.OrderTemplateDto;
import com.tupack.palletsortingapi.order.domain.OrderTemplate;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderTemplateRepository;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for managing order templates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderTemplateService {

  private final OrderTemplateRepository templateRepository;
  private final ClientRepository clientRepository;
  private final ObjectMapper objectMapper;

  /**
   * Create a new order template
   *
   * @param request Template creation request
   * @param userId  User ID
   * @return Created template ID
   */
  @Transactional
  public String createTemplate(CreateOrderTemplateRequest request, Long userId) {
    log.info("Creating order template '{}' for user ID: {}", request.getName(), userId);

    // Verify client exists
    Client client = clientRepository.findClientByUserId(userId)
        .orElseThrow(() -> new ClientNotFoundException("userId", userId));

    try {
      // Serialize items and route to JSON
      String itemsJson = objectMapper.writeValueAsString(request.getItems());
      String routeJson = objectMapper.writeValueAsString(request.getDefaultRoute());

      // Create template entity
      OrderTemplate template = OrderTemplate.builder()
          .client(client)
          .name(request.getName())
          .description(request.getDescription())
          .orderType(request.getOrderType())
          .itemsJson(itemsJson)
          .routeJson(routeJson)
          .createdAt(LocalDateTime.now())
          .build();

      // Save template
      OrderTemplate savedTemplate = templateRepository.save(template);

      log.info("Template created successfully with ID: {}", savedTemplate.getId());
      return "template-" + savedTemplate.getId();

    } catch (JsonProcessingException e) {
      log.error("Failed to serialize template data", e);
      throw new BusinessException("Failed to create template", "TEMPLATE_CREATION_ERROR");
    }
  }

  /**
   * Get all templates for a user
   *
   * @param userId User ID
   * @return List of user's templates
   */
  public List<OrderTemplateDto> getUserTemplates(Long userId) {
    log.info("Getting templates for user ID: {}", userId);

    // Verify client exists
    Client client = clientRepository.findClientByUserId(userId)
        .orElseThrow(() -> new ClientNotFoundException("userId", userId));

    // Get templates
    List<OrderTemplate> templates = templateRepository.findByClientIdOrderByLastUsedDesc(client.getId());

    // Convert to DTOs
    return templates.stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  /**
   * Convert template entity to DTO
   */
  private OrderTemplateDto toDto(OrderTemplate template) {
    try {
      return OrderTemplateDto.builder()
          .id("template-" + template.getId())
          .name(template.getName())
          .description(template.getDescription())
          .orderType(template.getOrderType())
          .items(objectMapper.readValue(template.getItemsJson(),
              objectMapper.getTypeFactory().constructCollectionType(List.class,
                  com.tupack.palletsortingapi.order.application.dto.EstimateItemDto.class)))
          .defaultRoute(objectMapper.readValue(template.getRouteJson(),
              com.tupack.palletsortingapi.order.application.dto.TemplateRouteDto.class))
          .createdAt(template.getCreatedAt())
          .lastUsed(template.getLastUsed())
          .build();
    } catch (JsonProcessingException e) {
      log.error("Failed to deserialize template data for template ID: {}", template.getId(), e);
      throw new BusinessException("Failed to load template", "TEMPLATE_DESERIALIZATION_ERROR");
    }
  }
}
