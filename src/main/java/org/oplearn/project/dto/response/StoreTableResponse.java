package org.oplearn.project.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.oplearn.project.entity.StoreTable;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StoreTableResponse {

  private Long id;
  private String zone;
  private String locationName;
  private String qrToken;
  private Boolean isActive;
  private Instant createdAt;

  public static StoreTableResponse from(StoreTable table) {
    if (table == null) return null;
    return StoreTableResponse.builder()
        .id(table.getId())
        .zone(table.getZone())
        .locationName(table.getLocationName())
        .qrToken(table.getQrToken())
        .isActive(table.getIsActive())
        .createdAt(table.getCreatedAt())
        .build();
  }
}
