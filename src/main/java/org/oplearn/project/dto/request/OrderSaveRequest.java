package org.oplearn.project.dto.request;

import lombok.*;
import org.oplearn.project.enums.OrderStatus;
import org.oplearn.project.enums.OrderType;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSaveRequest {

  private String orderCode;
  private OrderType orderType;
  private Long userId;
  private Long tableId;
  private String customerName;
  private String customerPhone;
  private String deliveryAddress;
  private BigDecimal subtotalAmount;
  private BigDecimal totalAmount;
  private String note;
  private OrderStatus status;
}
