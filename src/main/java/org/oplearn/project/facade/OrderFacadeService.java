package org.oplearn.project.facade;

import org.oplearn.project.dto.request.OrderCreateRequest;
import org.oplearn.project.dto.request.OrderStatusUpdateRequest;
import org.oplearn.project.dto.response.OrderResponse;
import org.oplearn.project.enums.OrderStatus;
import org.oplearn.project.enums.OrderType;

import java.util.List;

public interface OrderFacadeService {

  // Customer / Guest API
  OrderResponse create(OrderCreateRequest request);
  default OrderResponse createOrder(OrderCreateRequest request) {
    return create(request);
  }

  // Guest order tracking (Public)
  OrderResponse findByOrderCode(String orderCode);
  default OrderResponse getOrderByCode(String orderCode) {
    return findByOrderCode(orderCode);
  }

  // Registered user order history
  List<OrderResponse> getMyOrders();

  // Admin APIs
  List<OrderResponse> getAllOrders(OrderStatus status, OrderType orderType, Long tableId);

  OrderResponse detail(Long id);
  default OrderResponse getOrderById(Long id) {
    return detail(id);
  }

  OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request);

  OrderResponse removeOrderItem(Long orderId, Long itemId);

  void delete(Long id);
  default void deleteOrder(Long id) {
    delete(id);
  }
}
