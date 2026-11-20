package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.constants.OpLearnConstants.VariableConstant;
import org.oplearn.project.dto.request.ChangePasswordRequest;
import org.oplearn.project.dto.request.UserRequest;
import org.oplearn.project.dto.request.UserUpdateRequest;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.UserResponse;
import org.oplearn.project.entity.User;
import org.oplearn.project.exception.EmailAlreadyExistedException;
import org.oplearn.project.exception.PhoneNumberAlreadyExistedException;
import org.oplearn.project.exception.ProtectedAccountException;
import org.oplearn.project.exception.UserNotFoundException;
import org.oplearn.project.exception.UserUnauthorizedException;
import org.oplearn.project.exception.UsernameAlreadyExistedException;
import org.oplearn.project.repository.UserRepository;
import org.oplearn.project.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository repository;
  private final PasswordEncoder passwordEncoder;

  private static final Set<String> PROTECTED_USERNAMES = Set.of("admin", "superadmin");

  @Override
  @Transactional
  public UserResponse create(UserRequest request) {
    log.info("(create) user: {}", request.getUsername());
    if (repository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
      throw new UsernameAlreadyExistedException();
    }

    String phone = StringUtils.hasText(request.getPhoneNumber()) ? request.getPhoneNumber().trim() : null;
    if (phone != null && repository.existsByPhoneNumberAndIsDeletedFalse(phone)) {
      throw new PhoneNumberAlreadyExistedException();
    }

    User user = User.builder()
        .username(request.getUsername().trim())
        .fullName(StringUtils.hasText(request.getFullName()) ? request.getFullName().trim() : request.getUsername().trim())
        .password(passwordEncoder.encode(request.getPassword()))
        .phoneNumber(phone)
        .role(request.getRole() != null ? request.getRole() : org.oplearn.project.enums.UserRole.USER)
        .build();

    return UserResponse.from(repository.save(user));
  }

  @Override
  @Transactional
  public UserResponse update(UserUpdateRequest request, Long id) {
    log.info("(update) id: {}", id);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = authentication.getName();

    User currentUser = repository.findByUsernameAndIsDeletedFalse(currentUsername)
        .orElseThrow(UserNotFoundException::new);

    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    boolean isSelf = currentUser.getId().equals(id);

    if (!isAdmin && !isSelf) {
      log.warn("(update) user not authorized");
      throw new UserUnauthorizedException();
    }

    User user = repository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(UserNotFoundException::new);

    // Tài khoản hệ thống (admin/superadmin) chỉ chính chủ mới được sửa
    if (isProtectedAccount(user.getUsername()) && !isSelf) {
      log.warn("(update) protected account {} chỉ chính chủ được sửa", user.getUsername());
      throw new ProtectedAccountException();
    }

    if (!Objects.equals(user.getUsername(), request.getUsername().trim())
        && repository.existsByUsernameAndIsDeletedFalse(request.getUsername().trim())) {
      throw new UsernameAlreadyExistedException();
    }
    user.setUsername(request.getUsername().trim());
    if (request.getFullName() != null) {
      user.setFullName(request.getFullName().trim());
    }

    String phone = StringUtils.hasText(request.getPhoneNumber()) ? request.getPhoneNumber().trim() : null;
    if (phone != null && !Objects.equals(user.getPhoneNumber(), phone)
        && repository.existsByPhoneNumberAndIsDeletedFalse(phone)) {
      throw new PhoneNumberAlreadyExistedException();
    }
    user.setPhoneNumber(phone);
    // Chỉ đổi mật khẩu khi có nhập
    if (StringUtils.hasText(request.getPassword())) {
      user.setPassword(passwordEncoder.encode(request.getPassword()));
    }
    // Nâng/hạ vai trò: chỉ admin mới được đổi role
    if (request.getRole() != null && isAdmin) {
      user.setRole(request.getRole());
    }

    return UserResponse.from(repository.save(user));
  }

  private boolean isProtectedAccount(String username) {
    return username != null && PROTECTED_USERNAMES.contains(username.toLowerCase());
  }

  @Override
  public PageResponse<UserResponse> list(String keyword, int size, int page, boolean isAll) {
    Pageable pageable = isAll
        ? PageRequest.of(0, VariableConstant.MAX_ALL_SIZE)
        : PageRequest.of(page, size);

    Page<User> users = StringUtils.hasText(keyword)
        ? repository.search(keyword, pageable)
        : repository.findAllByIsDeletedFalse(pageable);

    return PageResponse.of(
        users.map(u -> UserResponse.from(u)).getContent(),
        (int) users.getTotalElements()
    );
  }

  @Override
  public UserResponse detail(Long id) {
    // Chống IDOR: chỉ chính chủ hoặc admin mới xem được profile của một id
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = repository.findByUsernameAndIsDeletedFalse(authentication.getName())
        .orElseThrow(UserNotFoundException::new);
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    if (!isAdmin && !currentUser.getId().equals(id)) {
      log.warn("(detail) user {} not authorized to view user {}", currentUser.getId(), id);
      throw new UserUnauthorizedException();
    }

    return repository.findByIdAndIsDeletedFalse(id)
        .map(u -> UserResponse.from(u))
        .orElseThrow(UserNotFoundException::new);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    User user = repository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(UserNotFoundException::new);
    if (isProtectedAccount(user.getUsername())) {
      log.warn("(delete) từ chối xoá tài khoản bảo vệ {}", user.getUsername());
      throw new ProtectedAccountException();
    }
    repository.softDeleteById(id);
  }

  @Override
  public User getUsernameOrThrow(String username) {
    return repository.findByUsernameAndIsDeletedFalse(username)
        .orElseThrow(UserNotFoundException::new);
  }

  @Override
  public User getAvailableUserAndThrow(Long id) {
    return repository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(UserNotFoundException::new);
  }

  private User getCurrentAuthenticatedUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
      throw new UserUnauthorizedException();
    }
    return repository.findByUsernameAndIsDeletedFalse(authentication.getName())
        .orElseThrow(UserNotFoundException::new);
  }

  @Override
  public UserResponse getMyProfile() {
    User currentUser = getCurrentAuthenticatedUser();
    return UserResponse.from(currentUser);
  }

  @Override
  @Transactional
  public void changePassword(ChangePasswordRequest request) {
    User currentUser = getCurrentAuthenticatedUser();
    log.info("(changePassword) user: {}", currentUser.getUsername());
    if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
      throw new org.oplearn.project.exception.base.BadRequestException("INVALID_OLD_PASSWORD", "Mật khẩu cũ không chính xác!");
    }
    currentUser.setPassword(passwordEncoder.encode(request.getNewPassword().trim()));
    repository.save(currentUser);
  }

  @Override
  @Transactional
  public UserResponse updateMyProfile(UserUpdateRequest request) {
    User currentUser = getCurrentAuthenticatedUser();
    log.info("(updateMyProfile) user: {}", currentUser.getUsername());
    if (StringUtils.hasText(request.getFullName())) {
      currentUser.setFullName(request.getFullName().trim());
    }
    if (request.getPhoneNumber() != null) {
      String phone = request.getPhoneNumber().trim();
      if (!phone.isBlank() && !Objects.equals(currentUser.getPhoneNumber(), phone) && repository.existsByPhoneNumberAndIsDeletedFalse(phone)) {
        throw new PhoneNumberAlreadyExistedException();
      }
      currentUser.setPhoneNumber(phone.isBlank() ? null : phone);
    }
    return UserResponse.from(repository.save(currentUser));
  }
}
