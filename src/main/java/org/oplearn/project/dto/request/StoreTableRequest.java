package org.oplearn.project.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class StoreTableRequest {

  @NotBlank(message = "table.zone.not_blank")
  private String zone;

  @NotBlank(message = "table.location_name.not_blank")
  @JsonAlias({"locationName", "location_name"})
  private String locationName;

  @JsonAlias({"isActive", "is_active"})
  private Boolean isActive;
}
