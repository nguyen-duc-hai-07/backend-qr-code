package org.oplearn.project.dto.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.oplearn.project.entity.Product;

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
public class ProductResponse {

  private Long id;
  private Long categoryId;
  private String categoryName;
  private String name;
  private String description;
  private BigDecimal price;
  private String imageUrl;
  private Boolean isAvailable;

  @Builder.Default
  private List<ProductToppingDTO> toppings = new ArrayList<>();

  private Instant createdAt;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static ProductResponse from(Product product) {
    if (product == null) return null;

    List<ProductToppingDTO> parsedToppings = new ArrayList<>();
    if (product.getToppings() != null && !product.getToppings().isBlank()) {
      try {
        parsedToppings = OBJECT_MAPPER.readValue(
            product.getToppings(),
            new TypeReference<List<ProductToppingDTO>>() {}
        );
      } catch (Exception ignored) {
      }
    }

    return ProductResponse.builder()
        .id(product.getId())
        .categoryId(product.getCategoryId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .imageUrl(product.getImageUrl())
        .isAvailable(product.getIsAvailable())
        .toppings(parsedToppings)
        .createdAt(product.getCreatedAt())
        .build();
  }

  public static ProductResponse from(Product product, String categoryName) {
    ProductResponse response = from(product);
    if (response != null) {
      response.setCategoryName(categoryName);
    }
    return response;
  }
}
