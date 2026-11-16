package org.oplearn.project.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.oplearn.project.entity.Order;
import org.oplearn.project.enums.OrderStatus;
import org.oplearn.project.enums.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderResponse {

  private Long id;
  private String orderCode;
  private OrderType orderType;
  private Long tableId;
  private String tableName;
  private String zone;
  private Long userId;
  private String customerName;
  private String customerPhone;
  private String deliveryAddress;
  private BigDecimal subtotalAmount;
  private BigDecimal totalAmount;
  private String note;
  private OrderStatus status;
  private Instant createdAt;

  @Builder.Default
  private List<OrderItemResponse> items = new ArrayList<>();

  public static OrderResponse from(Order order) {
    if (order == null) return null;
    return OrderResponse.builder()
        .id(order.getId())
        .orderCode(order.getOrderCode())
        .orderType(order.getOrderType())
        .tableId(order.getTableId())
        .userId(order.getUserId())
        .customerName(order.getCustomerName())
        .customerPhone(order.getCustomerPhone())
        .deliveryAddress(order.getDeliveryAddress())
        .subtotalAmount(order.getSubtotalAmount())
        .totalAmount(order.getTotalAmount())
        .note(order.getNote())
        .status(order.getStatus())
        .createdAt(order.getCreatedAt())
        .items(new ArrayList<>())
        .build();
  }

  public static OrderResponse from(Order order, String tableName, String zone, List<OrderItemResponse> items) {
    OrderResponse response = from(order);
    if (response != null) {
      response.setTableName(tableName);
      response.setZone(zone);
      if (items != null) {
        response.setItems(items);
      }
    }
    return response;
  }
}
