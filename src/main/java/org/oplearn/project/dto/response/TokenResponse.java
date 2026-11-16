package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class TokenResponse {
  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private long expiresIn;

  public TokenResponse withoutRefreshToken() {
    return TokenResponse.of(this.accessToken, null, this.tokenType, this.expiresIn);
  }
}
