package org.oplearn.project.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.User;
import org.oplearn.project.enums.UserRole;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserResponse {
  private Long id;
  private Instant createdAt;
  private String username;
  private String fullName;
  private String phoneNumber;
  private UserRole role;
  private Instant updatedAt;

  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getCreatedAt(),
        user.getUsername(),
        user.getFullName(),
        user.getPhoneNumber(),
        user.getRole(),
        user.getUpdatedAt()
    );
  }
}
