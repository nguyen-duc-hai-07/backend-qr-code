package org.oplearn.project.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.oplearn.project.enums.OrderType;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderCreateRequest {

  @NotNull(message = "order.type.not_null")
  private OrderType orderType;

  // Dành cho khách ngồi tại bàn (AT_TABLE)
  private Long tableId;
  private String qrToken;

  // Dành cho khách đặt ship (DELIVERY)
  private String customerName;
  private String customerPhone;
  private String deliveryAddress;

  private String note;

  @NotEmpty(message = "order.items.not_empty")
  @Valid
  private List<OrderItemRequest> items;
}
