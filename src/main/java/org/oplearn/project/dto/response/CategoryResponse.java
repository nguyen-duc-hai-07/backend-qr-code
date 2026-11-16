package org.oplearn.project.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.oplearn.project.entity.Category;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CategoryResponse {

  private Long id;
  private String name;
  private Integer displayOrder;
  private Boolean isActive;
  private Instant createdAt;

  public static CategoryResponse from(Category category) {
    if (category == null) return null;
    return CategoryResponse.builder()
        .id(category.getId())
        .name(category.getName())
        .displayOrder(category.getDisplayOrder())
        .isActive(category.getIsActive())
        .createdAt(category.getCreatedAt())
        .build();
  }
}
