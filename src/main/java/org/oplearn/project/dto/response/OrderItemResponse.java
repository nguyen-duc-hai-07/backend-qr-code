package org.oplearn.project.dto.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.oplearn.project.entity.OrderItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderItemResponse {

  private Long id;
  private Long productId;
  private String productName;
  private Integer quantity;
  private BigDecimal unitPrice;
  private List<OrderItemOptionResponse> options;
  private BigDecimal subtotal;
  private String note;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static OrderItemResponse from(OrderItem item) {
    if (item == null) return null;

    List<OrderItemOptionResponse> parsedOptions = new ArrayList<>();
    if (item.getOptions() != null && !item.getOptions().isBlank()) {
      try {
        parsedOptions = OBJECT_MAPPER.readValue(item.getOptions(), new TypeReference<List<OrderItemOptionResponse>>() {});
      } catch (Exception ignored) {
      }
    }

    return OrderItemResponse.builder()
        .id(item.getId())
        .productId(item.getProductId())
        .productName(item.getProductName())
        .quantity(item.getQuantity())
        .unitPrice(item.getUnitPrice())
        .options(parsedOptions)
        .subtotal(item.getSubtotal())
        .note(item.getNote())
        .build();
  }
}
