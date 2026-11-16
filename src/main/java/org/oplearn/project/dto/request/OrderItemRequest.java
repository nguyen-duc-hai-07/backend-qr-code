package org.oplearn.project.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderItemRequest {

  @NotNull(message = "order_item.product_id.not_null")
  private Long productId;

  @NotNull(message = "order_item.quantity.not_null")
  @Min(value = 1, message = "order_item.quantity.min_one")
  private Integer quantity;

  @Builder.Default
  private List<OrderItemOptionRequest> options = new ArrayList<>();

  private String note;
}
