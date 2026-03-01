package com.tupack.palletsortingapi.order.application.service;

import com.tupack.palletsortingapi.common.dto.GenericResponse;
import com.tupack.palletsortingapi.common.exception.BusinessException;
import com.tupack.palletsortingapi.order.application.dto.DispatcherDto;
import com.tupack.palletsortingapi.order.application.mapper.DispatcherDtoMapper;
import com.tupack.palletsortingapi.order.domain.Dispatcher;
import com.tupack.palletsortingapi.order.domain.Order;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.DispatcherRepository;
import com.tupack.palletsortingapi.order.infrastructure.outbound.database.OrderRepository;
import com.tupack.palletsortingapi.user.domain.Client;
import com.tupack.palletsortingapi.user.infrastructure.outbound.database.ClientRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DispatcherService {

  private final DispatcherRepository dispatcherRepository;
  private final DispatcherDtoMapper dispatcherDtoMapper;
  private final ClientRepository clientRepository;
  private final OrderRepository orderRepository;

  public GenericResponse getDispatchersByClient(Long clientId) {
    List<DispatcherDto> dispatchers = dispatcherRepository
        .findAllByEnabledAndClientId(true, clientId)
        .stream()
        .map(dispatcherDtoMapper::toDto)
        .toList();
    return GenericResponse.success(dispatchers);
  }

  @Transactional
  public GenericResponse createDispatcher(DispatcherDto dto) {
    Client client = clientRepository.findById(dto.clientId())
        .orElseThrow(() -> new BusinessException("Cliente no encontrado", "CLIENT_NOT_FOUND"));
    Dispatcher dispatcher = dispatcherDtoMapper.toEntity(dto);
    dispatcher.setClient(client);
    Dispatcher saved = dispatcherRepository.save(dispatcher);
    return GenericResponse.success(dispatcherDtoMapper.toDto(saved));
  }

  @Transactional
  public GenericResponse assignDispatcherToOrder(Long orderId, Long dispatcherId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new BusinessException("Orden no encontrada", "ORDER_NOT_FOUND"));
    Dispatcher dispatcher = dispatcherRepository.findById(dispatcherId)
        .orElseThrow(
            () -> new BusinessException("Despachador no encontrado", "DISPATCHER_NOT_FOUND"));
    order.setDispatcher(dispatcher);
    orderRepository.save(order);
    return GenericResponse.success(dispatcherDtoMapper.toDto(dispatcher));
  }
}
