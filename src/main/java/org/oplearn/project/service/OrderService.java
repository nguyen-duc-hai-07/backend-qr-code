package org.oplearn.project.service;

import org.oplearn.project.dto.request.OrderSaveRequest;
import org.oplearn.project.dto.response.OrderResponse;
import org.oplearn.project.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

  OrderResponse create(OrderSaveRequest request);

  OrderResponse detail(Long id);

  OrderResponse getByOrderCode(String orderCode);

  List<OrderResponse> getByUserId(Long userId);

  List<OrderResponse> getByTableId(Long tableId);

  List<OrderResponse> getByStatus(OrderStatus status);

  List<OrderResponse> getAll();

  OrderResponse updateStatus(Long id, OrderStatus status, String note);

  OrderResponse updateTotals(Long id, BigDecimal subtotalAmount, BigDecimal totalAmount, OrderStatus status);

  void delete(Long id);
}
