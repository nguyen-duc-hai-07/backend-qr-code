package org.oplearn.project.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.oplearn.project.enums.UserRole;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "password")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserRequest {
  @NotBlank(message = "user.username.not_blank")
  private String username;

  @JsonAlias({"fullName", "full_name"})
  private String fullName;

  @NotBlank(message = "user.password.not_blank")
  @Size(min = 8, message = "user.password.min_length")
  private String password;

  @JsonAlias({"phoneNumber", "phone_number"})
  private String phoneNumber;

  private UserRole role;
}
