package org.oplearn.project.service;

import org.oplearn.project.dto.request.OrderItemSaveRequest;
import org.oplearn.project.dto.response.OrderItemResponse;

import java.util.List;

public interface OrderItemService {

  List<OrderItemResponse> saveAll(List<OrderItemSaveRequest> items);

  OrderItemResponse save(OrderItemSaveRequest item);

  List<OrderItemResponse> getByOrderId(Long orderId);

  OrderItemResponse detail(Long id);

  void deleteItem(Long id);

  void deleteByOrderId(Long orderId);
}
