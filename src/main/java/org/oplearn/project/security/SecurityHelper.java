package org.oplearn.project.security;

import lombok.RequiredArgsConstructor;
import org.oplearn.project.entity.User;
import org.oplearn.project.exception.UserUnauthorizedException;
import org.oplearn.project.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityHelper {

  private final UserService userService;

  /**
   * Lấy User hiện tại đang đăng nhập. Nếu chưa đăng nhập (khách vãng lai) -> ném ngoại lệ 401.
   */
  public User currentUser() {
    return currentOptionalUser().orElseThrow(UserUnauthorizedException::new);
  }

  /**
   * Lấy User hiện tại nếu có. Nếu là khách vãng lai (chưa đăng nhập / anonymous) -> trả về Optional.empty().
   */
  public Optional<User> currentOptionalUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
      return Optional.empty();
    }
    try {
      return Optional.of(userService.getUsernameOrThrow(authentication.getName()));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public boolean isAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return false;
    }
    return authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
  }
}
