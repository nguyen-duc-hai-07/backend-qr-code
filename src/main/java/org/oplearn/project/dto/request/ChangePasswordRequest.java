package org.oplearn.project.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ChangePasswordRequest {

  @NotBlank(message = "Vui lòng nhập mật khẩu cũ")
  @JsonAlias({"oldPassword", "old_password", "currentPassword", "current_password"})
  private String oldPassword;

  @NotBlank(message = "Vui lòng nhập mật khẩu mới")
  @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
  @JsonAlias({"newPassword", "new_password"})
  private String newPassword;
}
