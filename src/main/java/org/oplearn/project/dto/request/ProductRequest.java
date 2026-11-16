package org.oplearn.project.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.oplearn.project.dto.response.ProductToppingDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ProductRequest {

  @JsonAlias({"categoryId", "category_id"})
  private Long categoryId;

  @NotBlank(message = "product.name.not_blank")
  private String name;

  private String description;

  @NotNull(message = "product.price.not_null")
  @DecimalMin(value = "0.0", message = "product.price.min_zero")
  private BigDecimal price;

  @JsonAlias({"imageUrl", "image_url"})
  private String imageUrl;

  @JsonAlias({"isAvailable", "is_available"})
  private Boolean isAvailable;

  @Builder.Default
  private List<ProductToppingDTO> toppings = new ArrayList<>();
}
