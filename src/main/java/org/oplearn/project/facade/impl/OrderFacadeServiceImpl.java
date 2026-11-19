package org.oplearn.project.facade.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.*;
import org.oplearn.project.dto.response.*;
import org.oplearn.project.entity.User;
import org.oplearn.project.enums.OrderStatus;
import org.oplearn.project.enums.OrderType;
import org.oplearn.project.exception.*;
import org.oplearn.project.facade.OrderFacadeService;
import org.oplearn.project.security.SecurityHelper;
import org.oplearn.project.service.OrderItemService;
import org.oplearn.project.service.OrderService;
import org.oplearn.project.service.ProductService;
import org.oplearn.project.service.StoreTableService;
import org.oplearn.project.websocket.EchoWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacadeServiceImpl implements OrderFacadeService {

  private final OrderService orderService;
  private final OrderItemService orderItemService;
  private final ProductService productService;
  private final StoreTableService storeTableService;
  private final SecurityHelper securityHelper;
  private final ObjectMapper objectMapper;
  private final EchoWebSocketHandler echoWebSocketHandler;

  @Override
  @Transactional
  public OrderResponse create(OrderCreateRequest request) {
    log.info("(OrderFacade) createOrder type: {}", request.getOrderType());

    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new OrderItemsEmptyException();
    }

    // Sinh mã đơn hàng dạng: ORD-261112-A1B2
    String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
    String randPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    String orderCode = String.format("ORD-%s-%s", datePart, randPart);

    Long tableId = null;
    Long userId = null;
    String customerName = request.getCustomerName();
    String customerPhone = request.getCustomerPhone();
    String deliveryAddress = null;
    String tableName = null;
    String tableZone = null;

    // 1. Phân loại theo từng đối tượng người dùng (chỉ có 2 loại: AT_TABLE và DELIVERY)
    if (OrderType.AT_TABLE.equals(request.getOrderType())) {
      // KHÁCH TẠI BÀN (Quét mã QR tại bàn hoặc gọi tại bàn)
      StoreTableResponse table;
      if (request.getQrToken() != null && !request.getQrToken().isBlank()) {
        table = storeTableService.getByQrToken(request.getQrToken());
      } else if (request.getTableId() != null) {
        table = storeTableService.detail(request.getTableId());
      } else {
        throw new TableRequiredException();
      }

      if (Boolean.FALSE.equals(table.getIsActive())) {
        throw new TableInactiveException();
      }

      tableId = table.getId();
      tableName = table.getLocationName();
      tableZone = table.getZone();

      // Khách vãng lai không bắt buộc đăng nhập (userId có thể null)
      userId = securityHelper.currentOptionalUser().map(User::getId).orElse(null);

      if (customerName == null || customerName.isBlank()) {
        customerName = table.getLocationName();
      }

    } else if (OrderType.DELIVERY.equals(request.getOrderType())) {
      // TÀI KHOẢN NGƯỜI DÙNG (USER) ĐẶT GIAO HÀNG (SHIP)
      User currentUser = securityHelper.currentOptionalUser()
          .orElseThrow(UserUnauthorizedException::new);

      userId = currentUser.getId();

      if (customerPhone == null || customerPhone.isBlank()) {
        customerPhone = currentUser.getPhoneNumber();
      }
      if (customerPhone == null || customerPhone.isBlank()) {
        throw new PhoneNumberRequiredException();
      }

      if (customerName == null || customerName.isBlank()) {
        customerName = currentUser.getFullName() != null && !currentUser.getFullName().isBlank()
            ? currentUser.getFullName()
            : currentUser.getUsername();
      }

      if (request.getDeliveryAddress() == null || request.getDeliveryAddress().isBlank()) {
        throw new DeliveryAddressRequiredException();
      }
      deliveryAddress = request.getDeliveryAddress();
    } else {
      throw new InvalidOrderTypeException();
    }

    // 2. Tính toán chi tiết từng món và tổng tiền
    BigDecimal subtotalAmount = BigDecimal.ZERO;
    List<OrderItemSaveRequest> itemsToSave = new ArrayList<>();

    for (OrderItemRequest itemReq : request.getItems()) {
      ProductResponse product = productService.detail(itemReq.getProductId());
      if (Boolean.FALSE.equals(product.getIsAvailable())) {
        throw new ProductUnavailableException(product.getName());
      }

      BigDecimal unitPrice = product.getPrice();
      BigDecimal extraPriceTotal = BigDecimal.ZERO;

      if (itemReq.getOptions() != null) {
        for (OrderItemOptionRequest opt : itemReq.getOptions()) {
          if (opt.getExtraPrice() != null) {
            extraPriceTotal = extraPriceTotal.add(opt.getExtraPrice());
          }
        }
      }

      BigDecimal itemUnitPrice = unitPrice.add(extraPriceTotal);
      BigDecimal itemSubtotal = itemUnitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
      subtotalAmount = subtotalAmount.add(itemSubtotal);

      String optionsJson = "[]";
      if (itemReq.getOptions() != null && !itemReq.getOptions().isEmpty()) {
        try {
          optionsJson = objectMapper.writeValueAsString(itemReq.getOptions());
        } catch (JsonProcessingException e) {
          log.warn("Could not serialize options to JSON", e);
        }
      }

      OrderItemSaveRequest orderItemReq = OrderItemSaveRequest.builder()
          .productId(product.getId())
          .productName(product.getName())
          .quantity(itemReq.getQuantity())
          .unitPrice(unitPrice)
          .options(optionsJson)
          .subtotal(itemSubtotal)
          .note(itemReq.getNote())
          .build();

      itemsToSave.add(orderItemReq);
    }

    BigDecimal totalAmount = subtotalAmount;

    // 3. Lưu đơn hàng thông qua OrderService bằng DTO
    OrderSaveRequest orderSaveReq = OrderSaveRequest.builder()
        .orderCode(orderCode)
        .orderType(request.getOrderType())
        .userId(userId)
        .tableId(tableId)
        .customerName(customerName)
        .customerPhone(customerPhone)
        .deliveryAddress(deliveryAddress)
        .subtotalAmount(subtotalAmount)
        .totalAmount(totalAmount)
        .note(request.getNote())
        .status(OrderStatus.PENDING)
        .build();

    OrderResponse savedOrder = orderService.create(orderSaveReq);

    // 4. Gán orderId và lưu các món thông qua OrderItemService bằng DTO
    for (OrderItemSaveRequest item : itemsToSave) {
      item.setOrderId(savedOrder.getId());
    }
    List<OrderItemResponse> itemResponses = orderItemService.saveAll(itemsToSave);

    savedOrder.setTableName(tableName);
    savedOrder.setZone(tableZone);
    savedOrder.setItems(itemResponses);

    // 5. Bắn WebSocket Real-time tới trang quản trị / quầy thu ngân / màn hình bếp
    String location = savedOrder.getTableName() != null
        ? savedOrder.getTableName()
        : "Khách giao hàng (" + savedOrder.getCustomerName() + ")";
    String notifyMessage = "Có đơn hàng mới từ " + location + " - Mã: " + savedOrder.getOrderCode();
    echoWebSocketHandler.broadcastOrderEvent("NEW_ORDER", savedOrder, notifyMessage);

    return savedOrder;
  }

  @Override
  public OrderResponse findByOrderCode(String orderCode) {
    log.info("(OrderFacade) getOrderByCode: {}", orderCode);
    OrderResponse order = orderService.getByOrderCode(orderCode);
    enrichOrderResponse(order);
    return order;
  }

  @Override
  public List<OrderResponse> getMyOrders() {
    User currentUser = securityHelper.currentUser();
    log.info("(OrderFacade) getMyOrders for user id: {}", currentUser.getId());
    List<OrderResponse> orders = orderService.getByUserId(currentUser.getId());
    orders.forEach(this::enrichOrderResponse);
    return orders;
  }

  @Override
  public List<OrderResponse> getAllOrders(OrderStatus status, OrderType orderType, Long tableId) {
    log.info("(OrderFacade) getAllOrders status: {}, orderType: {}, tableId: {}", status, orderType, tableId);
    List<OrderResponse> orders;
    if (status != null) {
      orders = orderService.getByStatus(status);
    } else if (tableId != null) {
      orders = orderService.getByTableId(tableId);
    } else {
      orders = orderService.getAll();
    }

    if (orderType != null) {
      orders = orders.stream()
          .filter(o -> orderType.equals(o.getOrderType()))
          .toList();
    }

    // Ưu tiên hiển thị: PREPARING (đang chế biến) lên đầu tiên, sau đó đến PENDING (chờ tiếp nhận)
    if (status == null) {
      Comparator<OrderResponse> statusPriorityComparator = Comparator
          .comparingInt((OrderResponse o) -> {
            if (OrderStatus.PREPARING.equals(o.getStatus())) return 1;
            if (OrderStatus.PENDING.equals(o.getStatus())) return 2;
            if (OrderStatus.COMPLETED.equals(o.getStatus())) return 3;
            if (OrderStatus.CANCELLED.equals(o.getStatus())) return 4;
            return 5;
          });
      orders = orders.stream().sorted(statusPriorityComparator).toList();
    }

    orders.forEach(this::enrichOrderResponse);
    return orders;
  }

  @Override
  public OrderResponse detail(Long id) {
    OrderResponse order = orderService.detail(id);
    enrichOrderResponse(order);
    return order;
  }

  @Override
  @Transactional
  public OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request) {
    log.info("(OrderFacade) updateOrderStatus order id: {}, status: {}", id, request.getStatus());
    OrderResponse updated = orderService.updateStatus(id, request.getStatus(), request.getNote());
    enrichOrderResponse(updated);

    // Bắn WebSocket Real-time cập nhật trạng thái đơn
    String notifyMessage = "Đơn hàng " + updated.getOrderCode() + " đã chuyển sang trạng thái " + updated.getStatus();
    echoWebSocketHandler.broadcastOrderEvent("ORDER_STATUS_UPDATED", updated, notifyMessage);

    if (updated.getUserId() != null) {
      echoWebSocketHandler.sendToUser(updated.getUserId(), "ORDER_STATUS_UPDATED", java.util.Map.of(
          "order", updated,
          "orderCode", updated.getOrderCode(),
          "status", updated.getStatus(),
          "message", notifyMessage
      ));
    }

    return updated;
  }

  @Override
  @Transactional
  public OrderResponse removeOrderItem(Long orderId, Long itemId) {
    log.info("(OrderFacade) removeOrderItem order id: {}, item id: {}", orderId, itemId);
    OrderResponse order = orderService.detail(orderId);

    if (!OrderStatus.PENDING.equals(order.getStatus()) && !OrderStatus.PREPARING.equals(order.getStatus())) {
      throw new OrderStatusNotPendingException();
    }

    orderItemService.deleteItem(itemId);

    List<OrderItemResponse> remainingItems = orderItemService.getByOrderId(orderId);

    OrderStatus newStatus = order.getStatus();
    BigDecimal newSubtotal = BigDecimal.ZERO;
    BigDecimal newTotal = BigDecimal.ZERO;

    if (remainingItems.isEmpty()) {
      newStatus = OrderStatus.CANCELLED;
    } else {
      newSubtotal = remainingItems.stream()
          .map(OrderItemResponse::getSubtotal)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

      newTotal = newSubtotal;
    }

    OrderResponse updatedOrder = orderService.updateTotals(orderId, newSubtotal, newTotal, newStatus);
    enrichOrderResponse(updatedOrder);

    // Bắn WebSocket Real-time khi món bị xóa/điều chỉnh
    String notifyMessage = "Đơn hàng " + updatedOrder.getOrderCode() + " vừa được điều chỉnh món";
    echoWebSocketHandler.broadcastOrderEvent("ORDER_ITEM_REMOVED", updatedOrder, notifyMessage);

    return updatedOrder;
  }

  @Override
  @Transactional
  public OrderResponse exchangeOrderItem(Long orderId, Long itemId, OrderItemRequest request) {
    log.info("(OrderFacade) exchangeOrderItem order id: {}, item id: {}, new product id: {}", orderId, itemId, request.getProductId());
    OrderResponse order = orderService.detail(orderId);

    if (!OrderStatus.PENDING.equals(order.getStatus()) && !OrderStatus.PREPARING.equals(order.getStatus())) {
      throw new OrderStatusNotPendingException();
    }

    ProductResponse product = productService.detail(request.getProductId());
    if (Boolean.FALSE.equals(product.getIsAvailable())) {
      throw new ProductUnavailableException(product.getName());
    }

    BigDecimal unitPrice = product.getPrice();
    BigDecimal extraPriceTotal = BigDecimal.ZERO;
    List<OrderItemOptionRequest> optionRequests = request.getOptions();

    if (optionRequests != null) {
      for (OrderItemOptionRequest opt : optionRequests) {
        if (opt.getExtraPrice() != null) {
          extraPriceTotal = extraPriceTotal.add(opt.getExtraPrice());
        }
      }
    }

    BigDecimal itemUnitPrice = unitPrice.add(extraPriceTotal);
    int qty = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;
    BigDecimal subtotal = itemUnitPrice.multiply(BigDecimal.valueOf(qty));

    String optionsJson = "[]";
    if (optionRequests != null && !optionRequests.isEmpty()) {
      try {
        optionsJson = objectMapper.writeValueAsString(optionRequests);
      } catch (JsonProcessingException e) {
        log.warn("Could not serialize options to JSON", e);
      }
    }

    // 1. Soft delete old item
    orderItemService.deleteItem(itemId);

    // 2. Save replacement item
    OrderItemSaveRequest newItem = OrderItemSaveRequest.builder()
        .orderId(orderId)
        .productId(product.getId())
        .productName(product.getName())
        .quantity(qty)
        .unitPrice(unitPrice)
        .options(optionsJson)
        .subtotal(subtotal)
        .note(request.getNote())
        .build();

    orderItemService.save(newItem);

    // 3. Recalculate totals
    List<OrderItemResponse> allItems = orderItemService.getByOrderId(orderId);
    BigDecimal newSubtotal = allItems.stream()
        .map(OrderItemResponse::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal newTotal = newSubtotal;

    OrderResponse updatedOrder = orderService.updateTotals(orderId, newSubtotal, newTotal, order.getStatus());
    enrichOrderResponse(updatedOrder);

    String notifyMessage = "Đơn hàng " + updatedOrder.getOrderCode() + " vừa được đổi sang món " + product.getName();
    echoWebSocketHandler.broadcastOrderEvent("ORDER_ITEM_EXCHANGED", updatedOrder, notifyMessage);

    return updatedOrder;
  }

  @Override
  @Transactional
  public OrderResponse addOrderItem(Long orderId, OrderItemRequest request) {
    log.info("(OrderFacade) addOrderItem order id: {}, product id: {}", orderId, request.getProductId());
    OrderResponse order = orderService.detail(orderId);

    if (!OrderStatus.PENDING.equals(order.getStatus()) && !OrderStatus.PREPARING.equals(order.getStatus())) {
      throw new OrderStatusNotPendingException();
    }

    ProductResponse product = productService.detail(request.getProductId());
    if (Boolean.FALSE.equals(product.getIsAvailable())) {
      throw new ProductUnavailableException(product.getName());
    }

    BigDecimal unitPrice = product.getPrice();
    BigDecimal extraPriceTotal = BigDecimal.ZERO;
    List<OrderItemOptionRequest> optionRequests = request.getOptions();

    if (optionRequests != null) {
      for (OrderItemOptionRequest opt : optionRequests) {
        if (opt.getExtraPrice() != null) {
          extraPriceTotal = extraPriceTotal.add(opt.getExtraPrice());
        }
      }
    }

    BigDecimal itemUnitPrice = unitPrice.add(extraPriceTotal);
    int qty = request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1;
    BigDecimal subtotal = itemUnitPrice.multiply(BigDecimal.valueOf(qty));

    String optionsJson = "[]";
    if (optionRequests != null && !optionRequests.isEmpty()) {
      try {
        optionsJson = objectMapper.writeValueAsString(optionRequests);
      } catch (JsonProcessingException e) {
        log.warn("Could not serialize options to JSON", e);
      }
    }

    OrderItemSaveRequest newItem = OrderItemSaveRequest.builder()
        .orderId(orderId)
        .productId(product.getId())
        .productName(product.getName())
        .quantity(qty)
        .unitPrice(unitPrice)
        .options(optionsJson)
        .subtotal(subtotal)
        .note(request.getNote())
        .build();

    orderItemService.save(newItem);

    List<OrderItemResponse> allItems = orderItemService.getByOrderId(orderId);
    BigDecimal newSubtotal = allItems.stream()
        .map(OrderItemResponse::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal newTotal = newSubtotal;

    OrderResponse updatedOrder = orderService.updateTotals(orderId, newSubtotal, newTotal, order.getStatus());
    enrichOrderResponse(updatedOrder);

    String notifyMessage = "Đơn hàng " + updatedOrder.getOrderCode() + " vừa được thêm món " + product.getName();
    echoWebSocketHandler.broadcastOrderEvent("ORDER_ITEM_ADDED", updatedOrder, notifyMessage);

    return updatedOrder;
  }

  @Override
  @Transactional
  public void delete(Long id) {
    log.info("(OrderFacade) deleteOrder id: {}", id);
    orderService.delete(id);
    orderItemService.deleteByOrderId(id);
  }

  private void enrichOrderResponse(OrderResponse order) {
    if (order == null) return;

    if (order.getTableId() != null) {
      try {
        StoreTableResponse table = storeTableService.detail(order.getTableId());
        order.setTableName(table.getLocationName());
        order.setZone(table.getZone());
      } catch (Exception ignored) {
      }
    }

    List<OrderItemResponse> items = orderItemService.getByOrderId(order.getId());
    order.setItems(items);
  }
}
