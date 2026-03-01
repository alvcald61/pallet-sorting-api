package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.notification.domain.event.OrderCreatedEvent;
import com.tupack.palletsortingapi.order.application.dto.PalletBulkDto;
import com.tupack.palletsortingapi.order.application.dto.SolvePackingRequest;
import com.tupack.palletsortingapi.order.domain.Bulk;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.domain.OrderPallet;
import com.tupack.palletsortingapi.order.domain.Pallet;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.BulkRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderPalletRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.PalletRepository;
import com.tupack.palletsortingapi.utils.PackingType;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for persisting orders and their related entities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPersistenceService {

  private final OrderRepository orderRepository;
  private final OrderPalletRepository orderPalletRepository;
  private final BulkRepository bulkRepository;
  private final PalletRepository palletRepository;
  private final OrderStatusService orderStatusService;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Persist order and all related entities (pallets or bulks)
   *
   * @param order       Order to persist
   * @param packingType Packing type (2D, 3D, BULK)
   * @param request     Original request with pallet/bulk data
   * @return Persisted order with ID
   */
  @Transactional
  public Order persistOrder(Order order, String packingType, SolvePackingRequest request) {
    // Save order first to get ID
    Order savedOrder = orderRepository.save(order);

    // Record initial status
    orderStatusService.recordStatus(savedOrder);

    // Save cargo items based on packing type
    if (packingType.equals(PackingType.BULK.getName())) {
      saveBulks(request, savedOrder);
    } else {
      savePallets(request, savedOrder);
    }

    // Publish OrderCreatedEvent
    eventPublisher.publishEvent(new OrderCreatedEvent(this, savedOrder));
    log.info("Published OrderCreatedEvent for order: {}", savedOrder.getId());

    return savedOrder;
  }

  /**
   * Save pallets for the order (for 2D and 3D packing)
   */
  private void savePallets(SolvePackingRequest request, Order order) {
    List<OrderPallet> orderPallets = request.getPallets().stream()
        .map(palletDto -> mapToOrderPallet(palletDto, order))
        .toList();

    List<OrderPallet> savedPallets = orderPalletRepository.saveAll(orderPallets);
    order.setOrderPallets(savedPallets);
  }

  /**
   * Save bulks for the order (for bulk packing)
   */
  private void saveBulks(SolvePackingRequest request, Order order) {
    List<Bulk> bulkList = request.getPallets().stream()
        .map(bulkDto -> new Bulk(
            order,
            bulkDto.getQuantity(),
            bulkDto.getVolume(),
            bulkDto.getWeight(),
            bulkDto.getHeight()
        ))
        .toList();

    List<Bulk> savedBulks = bulkRepository.saveAll(bulkList);
    order.setBulkList(savedBulks);
  }

  /**
   * Map DTO to OrderPallet entity
   */
  private OrderPallet mapToOrderPallet(PalletBulkDto dto, Order order) {
    // Find or create pallet template
    Pallet pallet = palletRepository
        .findByWidthAndLengthAndHeight(dto.getWidth(), dto.getLength(), dto.getHeight())
        .orElseGet(() -> {
          Pallet newPallet = new Pallet();
          newPallet.setWidth(dto.getWidth());
          newPallet.setLength(dto.getLength());
          newPallet.setHeight(dto.getHeight());
          return palletRepository.save(newPallet);
        });

    // Create order pallet
    OrderPallet orderPallet = new OrderPallet();
    orderPallet.setPallet(pallet);
    orderPallet.setOrder(order);
    orderPallet.setQuantity(dto.getQuantity());
    orderPallet.setWeight(BigDecimal.valueOf(dto.getWeight()));

    return orderPallet;
  }

  /**
   * Update an existing order
   *
   * @param order Updated order
   * @return Saved order
   */
  @Transactional
  public Order updateOrder(Order order) {
    return orderRepository.save(order);
  }

  /**
   * Delete order and related entities
   *
   * @param orderId Order ID to delete
   */
  @Transactional
  public void deleteOrder(Long orderId) {
    orderRepository.deleteById(orderId);
  }
}
