package org.oplearn.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.CategoryRequest;
import org.oplearn.project.dto.response.CategoryMenuResponse;
import org.oplearn.project.dto.response.CategoryResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.facade.CategoryFacadeService;
import org.oplearn.project.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.CREATED_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.SUCCESS_MESSAGE;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

  private final CategoryFacadeService facade;
  private final CategoryService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseGeneral<CategoryResponse> create(
      @Valid @RequestBody CategoryRequest request
  ) {
    log.info("(create) category: {}", request.getName());
    return ResponseGeneral.ofCreated(CREATED_MESSAGE, facade.create(request));
  }

  @PutMapping("/{id}")
  public ResponseGeneral<CategoryResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody CategoryRequest request
  ) {
    log.info("(update) id: {}, category: {}", id, request.getName());
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseGeneral<Void> delete(@PathVariable Long id) {
    log.info("(delete) id: {}", id);
    facade.delete(id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  @GetMapping("/{id}")
  public ResponseGeneral<CategoryResponse> detail(@PathVariable Long id) {
    log.info("(detail) id: {}", id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.detail(id));
  }

  /**
   * Lấy danh sách danh mục:
   * - all = false (mặc định cho khách): chỉ lấy danh mục đang hoạt động.
   * - all = true (cho quản trị admin): lấy tất cả danh mục.
   */
  @GetMapping
  public ResponseGeneral<List<CategoryResponse>> getAll(
      @RequestParam(value = "all", defaultValue = "false") boolean all
  ) {
    log.info("(getAll) all: {}", all);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.getAll(!all));
  }

  /**
   * Thực đơn công khai (gom nhóm danh mục kèm các món ăn).
   */
  @GetMapping("/menu")
  public ResponseGeneral<List<CategoryMenuResponse>> getPublicMenu() {
    log.info("(getPublicMenu) fetch public menu");
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.getPublicMenu());
  }
}
