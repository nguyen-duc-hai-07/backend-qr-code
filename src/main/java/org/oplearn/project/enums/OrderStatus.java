package org.oplearn.project.enums;

public enum OrderStatus {
  PENDING,     // Yêu cầu mới đặt / Chờ tiếp nhận
  PREPARING,   // Đang cần chế biến / Đang nấu, pha chế tại quầy
  COMPLETED,   // Đã hoàn thành chế biến, phục vụ và thanh toán
  CANCELLED    // Đã hủy
}
