package org.oplearn.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.OrderCreateRequest;
import org.oplearn.project.dto.request.OrderStatusUpdateRequest;
import org.oplearn.project.dto.response.OrderResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.enums.OrderStatus;
import org.oplearn.project.enums.OrderType;
import org.oplearn.project.facade.OrderFacadeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.CREATED_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.SUCCESS_MESSAGE;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final OrderFacadeService facade;

  // ====================================================================
  // 1. PUBLIC & USER APIs: Đặt món & Theo dõi đơn hàng
  // ====================================================================

  /**
   * Đặt món chung:
   * - Khách gọi món tại bàn (AT_TABLE) -> không cần đăng nhập.
   * - Người dùng (USER): đặt giao hàng (DELIVERY) -> yêu cầu đăng nhập tài khoản.
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseGeneral<OrderResponse> create(
      @Valid @RequestBody OrderCreateRequest request
  ) {
    log.info("(create) orderType: {}", request.getOrderType());
    return ResponseGeneral.ofCreated(CREATED_MESSAGE, facade.create(request));
  }

  /**
   * Dành cho Khách vãng lai: Tra cứu tiến độ đơn hàng bằng mã đơn (orderCode) mà không cần đăng nhập.
   */
  @GetMapping("/track/{orderCode}")
  public ResponseGeneral<OrderResponse> findByOrderCode(
      @PathVariable String orderCode
  ) {
    log.info("(findByOrderCode) code: {}", orderCode);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.findByOrderCode(orderCode));
  }

  // ====================================================================
  // 2. USER APIs: Xem lịch sử đơn hàng của tài khoản đã đăng nhập
  // ====================================================================

  /**
   * Người dùng đã đăng nhập xem lại danh sách đơn hàng đã đặt giao hàng của mình.
   */
  @GetMapping("/my-orders")
  public ResponseGeneral<List<OrderResponse>> getMyOrders() {
    log.info("(getMyOrders) get current user order history");
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.getMyOrders());
  }

  // ====================================================================
  // 3. ADMIN APIs: Quản lý, xác nhận, cập nhật đơn hàng & chỉnh sửa món
  // ====================================================================

  /**
   * Admin / Quầy thu ngân / Bếp xem danh sách đơn hàng (lọc theo trạng thái, loại đơn, bàn).
   */
  @GetMapping
  public ResponseGeneral<List<OrderResponse>> getAll(
      @RequestParam(value = "status", required = false) OrderStatus status,
      @RequestParam(value = "order_type", required = false) OrderType orderType,
      @RequestParam(value = "table_id", required = false) Long tableId
  ) {
    log.info("(getAll) status: {}, orderType: {}, tableId: {}", status, orderType, tableId);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.getAllOrders(status, orderType, tableId));
  }

  /**
   * Admin xem chi tiết đơn hàng.
   */
  @GetMapping("/{id}")
  public ResponseGeneral<OrderResponse> detail(
      @PathVariable Long id
  ) {
    log.info("(detail) id: {}", id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.detail(id));
  }

  /**
   * Admin cập nhật trạng thái đơn hàng (COMPLETED, CANCELLED).
   */
  @PutMapping("/{id}/status")
  public ResponseGeneral<OrderResponse> updateStatus(
      @PathVariable Long id,
      @Valid @RequestBody OrderStatusUpdateRequest request
  ) {
    log.info("(updateStatus) id: {}, status: {}", id, request.getStatus());
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.updateOrderStatus(id, request));
  }

  /**
   * Admin xóa món bị sai hoặc khách muốn hủy một món trong lúc đơn còn PENDING.
   * Hệ thống tự động tính lại tổng tiền đơn hàng.
   */
  @DeleteMapping("/{orderId}/items/{itemId}")
  public ResponseGeneral<OrderResponse> removeOrderItem(
      @PathVariable Long orderId,
      @PathVariable Long itemId
  ) {
    log.info("(removeOrderItem) orderId: {}, itemId: {}", orderId, itemId);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.removeOrderItem(orderId, itemId));
  }

  /**
   * Admin xóa đơn hàng (Soft Delete: is_deleted = true).
   */
  @DeleteMapping("/{id}")
  public ResponseGeneral<Void> delete(
      @PathVariable Long id
  ) {
    log.info("(delete) id: {}", id);
    facade.delete(id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }
}
