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
@Table(name = "products")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product extends BaseEntity {

  @Column(name = "category_id")
  private Long categoryId;

  @Column(name = "name", length = 255, nullable = false)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "price", precision = 15, scale = 2, nullable = false)
  private BigDecimal price;

  @Column(name = "image_url", columnDefinition = "TEXT")
  private String imageUrl;

  @Column(name = "is_available", nullable = false)
  @Builder.Default
  private Boolean isAvailable = Boolean.TRUE;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "toppings", columnDefinition = "jsonb")
  @Builder.Default
  private String toppings = "[]";
}
