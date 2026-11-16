package org.oplearn.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.ProductAvailabilityRequest;
import org.oplearn.project.dto.request.ProductRequest;
import org.oplearn.project.dto.response.ProductResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.facade.ProductFacadeService;
import org.oplearn.project.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.CREATED_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.SUCCESS_MESSAGE;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

  private final ProductFacadeService facade;
  private final ProductService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseGeneral<ProductResponse> create(
      @Valid @RequestBody ProductRequest request
  ) {
    log.info("(create) product: {}", request.getName());
    return ResponseGeneral.ofCreated(CREATED_MESSAGE, facade.create(request));
  }

  @PutMapping("/{id}")
  public ResponseGeneral<ProductResponse> update(
      @PathVariable Long id,
      @Valid @RequestBody ProductRequest request
  ) {
    log.info("(update) id: {}, product: {}", id, request.getName());
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.update(id, request));
  }

  /**
   * Cập nhật trạng thái Còn hàng / Hết hàng của món ăn (is_available).
   * Tự động bắn WebSocket action = "PRODUCT_AVAILABILITY_CHANGED" để đồng bộ ngay lập tức
   * tới tất cả khách hàng đang xem thực đơn và màn hình nhân viên/quản lý.
   */
  @PatchMapping("/{id}/availability")
  public ResponseGeneral<ProductResponse> updateAvailability(
      @PathVariable Long id,
      @Valid @RequestBody ProductAvailabilityRequest request
  ) {
    log.info("(updateAvailability) id: {}, isAvailable: {}", id, request.getIsAvailable());
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.updateAvailability(id, request.getIsAvailable()));
  }

  @DeleteMapping("/{id}")
  public ResponseGeneral<Void> delete(@PathVariable Long id) {
    log.info("(delete) id: {}", id);
    facade.delete(id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  @GetMapping("/{id}")
  public ResponseGeneral<ProductResponse> detail(@PathVariable Long id) {
    log.info("(detail) id: {}", id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.detail(id));
  }

  /**
   * Lấy danh sách sản phẩm:
   * - keyword: tìm kiếm theo tên hoặc mô tả món ăn.
   * - category_id: lọc theo danh mục cụ thể.
   * - all = false (mặc định): chỉ lấy các món đang có sẵn phục vụ (is_available = true).
   * - all = true (cho quản trị admin): lấy tất cả món ăn.
   */
  @GetMapping
  public ResponseGeneral<List<ProductResponse>> getAll(
      @RequestParam(value = "keyword", required = false) String keyword,
      @RequestParam(value = "category_id", required = false) Long categoryId,
      @RequestParam(value = "all", defaultValue = "false") boolean all
  ) {
    log.info("(getAll) keyword: {}, categoryId: {}, all: {}", keyword, categoryId, all);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.getAll(keyword, categoryId, !all));
  }
}
