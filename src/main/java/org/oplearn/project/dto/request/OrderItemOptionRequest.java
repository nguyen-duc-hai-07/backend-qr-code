package org.oplearn.project.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OrderItemOptionRequest {

  private String name;

  @JsonAlias({"price", "extra_price", "extraPrice"})
  @Builder.Default
  private BigDecimal extraPrice = BigDecimal.ZERO;
}
