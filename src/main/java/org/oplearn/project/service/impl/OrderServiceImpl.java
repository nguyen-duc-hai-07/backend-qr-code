package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.OrderSaveRequest;
import org.oplearn.project.dto.response.OrderResponse;
import org.oplearn.project.entity.Order;
import org.oplearn.project.enums.OrderStatus;
import org.oplearn.project.exception.InvalidOrderStatusException;
import org.oplearn.project.exception.OrderNotFoundException;
import org.oplearn.project.repository.OrderRepository;
import org.oplearn.project.service.OrderService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;

  @Override
  @Transactional
  public OrderResponse create(OrderSaveRequest request) {
    log.info("(create) order code: {}", request.getOrderCode());

    Order order = Order.builder()
        .orderCode(request.getOrderCode())
        .orderType(request.getOrderType())
        .userId(request.getUserId())
        .tableId(request.getTableId())
        .customerName(request.getCustomerName())
        .customerPhone(request.getCustomerPhone())
        .deliveryAddress(request.getDeliveryAddress())
        .subtotalAmount(request.getSubtotalAmount())
        .totalAmount(request.getTotalAmount())
        .note(request.getNote())
        .status(request.getStatus() != null ? request.getStatus() : OrderStatus.PENDING)
        .build();

    Order saved = orderRepository.save(order);
    return OrderResponse.from(saved);
  }

  @Override
  public OrderResponse detail(Long id) {
    Order order = orderRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(OrderNotFoundException::new);
    return OrderResponse.from(order);
  }

  @Override
  public OrderResponse getByOrderCode(String orderCode) {
    Order order = orderRepository.findByOrderCodeAndIsDeletedFalse(orderCode)
        .orElseThrow(OrderNotFoundException::new);
    return OrderResponse.from(order);
  }

  @Override
  public List<OrderResponse> getByUserId(Long userId) {
    return orderRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId).stream()
        .map(order -> OrderResponse.from(order))
        .toList();
  }

  @Override
  public List<OrderResponse> getByTableId(Long tableId) {
    return orderRepository.findByTableIdAndIsDeletedFalseOrderByCreatedAtDesc(tableId).stream()
        .map(order -> OrderResponse.from(order))
        .toList();
  }

  @Override
  public List<OrderResponse> getByStatus(OrderStatus status) {
    return orderRepository.findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(status).stream()
        .map(order -> OrderResponse.from(order))
        .toList();
  }

  @Override
  public List<OrderResponse> getAll() {
    return orderRepository.findAllByIsDeletedFalse(
        Sort.by(Sort.Direction.DESC, "createdAt")
    ).stream()
        .map(order -> OrderResponse.from(order))
        .toList();
  }

  @Override
  @Transactional
  public OrderResponse updateStatus(Long id, OrderStatus status, String note) {
    log.info("(updateStatus) order id: {}, status: {}", id, status);
    Order order = orderRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(OrderNotFoundException::new);

    // Không cho phép cập nhật trạng thái nếu đơn đã hoàn thành hoặc đã bị hủy
    if (order.getStatus() != status && (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED)) {
      throw new InvalidOrderStatusException();
    }

    order.setStatus(status);
    if (note != null && !note.isBlank()) {
      order.setNote(note);
    }
    Order saved = orderRepository.save(order);
    return OrderResponse.from(saved);
  }

  @Override
  @Transactional
  public OrderResponse updateTotals(Long id, BigDecimal subtotalAmount, BigDecimal totalAmount, OrderStatus status) {
    log.info("(updateTotals) order id: {}, subtotal: {}, total: {}, status: {}", id, subtotalAmount, totalAmount, status);
    Order order = orderRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(OrderNotFoundException::new);

    order.setSubtotalAmount(subtotalAmount);
    order.setTotalAmount(totalAmount);
    if (status != null) {
      order.setStatus(status);
    }
    Order saved = orderRepository.save(order);
    return OrderResponse.from(saved);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    log.info("(delete) order id: {}", id);
    Order order = orderRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(OrderNotFoundException::new);
    order.setIsDeleted(true);
    orderRepository.save(order);
  }
}
