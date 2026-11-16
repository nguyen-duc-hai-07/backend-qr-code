package org.oplearn.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;
import org.oplearn.project.entity.base.BaseEntity;
import org.oplearn.project.enums.OrderStatus;
import org.oplearn.project.enums.OrderType;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order extends BaseEntity {

  @Column(name = "order_code", length = 50, nullable = false, unique = true)
  private String orderCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "order_type", length = 50, nullable = false)
  private OrderType orderType;

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "table_id")
  private Long tableId;

  @Column(name = "customer_name", length = 150)
  private String customerName;

  @Column(name = "customer_phone", length = 50)
  private String customerPhone;

  @Column(name = "delivery_address", columnDefinition = "TEXT")
  private String deliveryAddress;

  @Column(name = "subtotal_amount", precision = 15, scale = 2, nullable = false)
  @Builder.Default
  private BigDecimal subtotalAmount = BigDecimal.ZERO;

  @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
  @Builder.Default
  private BigDecimal totalAmount = BigDecimal.ZERO;

  @Column(name = "note", columnDefinition = "TEXT")
  private String note;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 50, nullable = false)
  @Builder.Default
  private OrderStatus status = OrderStatus.PENDING;
}
