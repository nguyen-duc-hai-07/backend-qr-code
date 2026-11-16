package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.OrderItemSaveRequest;
import org.oplearn.project.dto.response.OrderItemResponse;
import org.oplearn.project.entity.OrderItem;
import org.oplearn.project.exception.OrderItemNotFoundException;
import org.oplearn.project.repository.OrderItemRepository;
import org.oplearn.project.service.OrderItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

  private final OrderItemRepository orderItemRepository;

  @Override
  @Transactional
  public List<OrderItemResponse> saveAll(List<OrderItemSaveRequest> requests) {
    List<OrderItem> items = requests.stream()
        .map(req -> OrderItem.builder()
            .orderId(req.getOrderId())
            .productId(req.getProductId())
            .productName(req.getProductName())
            .quantity(req.getQuantity())
            .unitPrice(req.getUnitPrice())
            .options(req.getOptions())
            .subtotal(req.getSubtotal())
            .note(req.getNote())
            .build())
        .toList();

    return orderItemRepository.saveAll(items).stream()
        .map(item -> OrderItemResponse.from(item))
        .toList();
  }

  @Override
  @Transactional
  public OrderItemResponse save(OrderItemSaveRequest req) {
    OrderItem item = OrderItem.builder()
        .orderId(req.getOrderId())
        .productId(req.getProductId())
        .productName(req.getProductName())
        .quantity(req.getQuantity())
        .unitPrice(req.getUnitPrice())
        .options(req.getOptions())
        .subtotal(req.getSubtotal())
        .note(req.getNote())
        .build();

    return OrderItemResponse.from(orderItemRepository.save(item));
  }

  @Override
  public List<OrderItemResponse> getByOrderId(Long orderId) {
    return orderItemRepository.findByOrderIdAndIsDeletedFalse(orderId).stream()
        .map(item -> OrderItemResponse.from(item))
        .toList();
  }

  @Override
  public OrderItemResponse detail(Long id) {
    OrderItem item = orderItemRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(OrderItemNotFoundException::new);
    return OrderItemResponse.from(item);
  }

  @Override
  @Transactional
  public void deleteItem(Long id) {
    OrderItem item = orderItemRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(OrderItemNotFoundException::new);
    item.setIsDeleted(true);
    orderItemRepository.save(item);
  }

  @Override
  @Transactional
  public void deleteByOrderId(Long orderId) {
    log.info("(deleteByOrderId) order id: {}", orderId);
    List<OrderItem> items = orderItemRepository.findByOrderIdAndIsDeletedFalse(orderId);
    for (OrderItem item : items) {
      item.setIsDeleted(true);
    }
    orderItemRepository.saveAll(items);
  }
}
