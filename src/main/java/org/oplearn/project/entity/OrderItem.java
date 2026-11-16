package org.oplearn.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.oplearn.project.entity.base.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem extends BaseEntity {

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "product_id")
  private Long productId;

  @Column(name = "product_name", length = 255, nullable = false)
  private String productName;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
  private BigDecimal unitPrice;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "options", columnDefinition = "jsonb")
  @Builder.Default
  private String options = "[]";

  @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
  private BigDecimal subtotal;

  @Column(name = "note", length = 255)
  private String note;
}
