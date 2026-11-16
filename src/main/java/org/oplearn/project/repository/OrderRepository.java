package org.oplearn.project.repository;

import org.oplearn.project.entity.Order;
import org.oplearn.project.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

  Optional<Order> findByIdAndIsDeletedFalse(Long id);

  Optional<Order> findByOrderCodeAndIsDeletedFalse(String orderCode);

  List<Order> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

  List<Order> findByTableIdAndIsDeletedFalseOrderByCreatedAtDesc(Long tableId);

  List<Order> findByStatusAndIsDeletedFalseOrderByCreatedAtDesc(OrderStatus status);

  List<Order> findAllByIsDeletedFalse();

  List<Order> findAllByIsDeletedFalse(Sort sort);

  List<Order> findAllByIsDeletedFalseOrderByCreatedAtDesc();

  Page<Order> findAllByIsDeletedFalse(Pageable pageable);

  // ====================================================================
  // ANALYTICS & REVENUE PROJECTIONS & NATIVE QUERIES (Chuẩn backend-e-commerce)
  // ====================================================================

  interface OrderSummaryProjection {
    Long getTotalOrders();
    Long getCompletedOrders();
    Long getCancelledOrders();
    Long getRefundedOrders();
    Long getInProgressOrders();
    BigDecimal getTotalRevenue();
  }

  interface OrderTimelineProjection {
    LocalDate getPeriod();
    BigDecimal getRevenue();
    Long getOrderCount();
  }

  /**
   * 1. Query 5 thẻ KPI Tổng quan: Single-pass scan bằng COUNT(*) FILTER và SUM(total_amount).
   * Lọc duy nhất các đơn trong khoảng thời gian [fromDate, toDate).
   * inProgressOrders đếm các đơn PENDING và PREPARING (Đang cần chế biến!).
   */
  @Query(value = """
      SELECT COUNT(*) AS totalOrders,
             COUNT(*) FILTER (WHERE o.status = 'COMPLETED') AS completedOrders,
             COUNT(*) FILTER (WHERE o.status = 'CANCELLED') AS cancelledOrders,
             0 AS refundedOrders,
             COUNT(*) FILTER (WHERE o.status IN ('PENDING', 'PREPARING')) AS inProgressOrders,
             COALESCE(SUM(o.total_amount) FILTER (WHERE o.status = 'COMPLETED'), 0) AS totalRevenue
      FROM orders o
      WHERE o.is_deleted = false
        AND o.created_at >= :fromDate AND o.created_at < :toDate
  """, nativeQuery = true)
  OrderSummaryProjection getOrderSummaryStatistics(
      @Param("fromDate") Instant fromDate,
      @Param("toDate") Instant toDate
  );

  /**
   * 2. Query Biểu đồ Doanh thu theo Tháng (12 tháng đầy đủ bằng generate_series & Zero-filling).
   */
  @Query(value = """
      SELECT CAST(m.period AS date) AS period,
             COALESCE(SUM(o.total_amount), 0) AS revenue,
             COUNT(o.id) AS orderCount
      FROM generate_series(
             date_trunc('month', timezone('Asia/Ho_Chi_Minh', CAST(:fromDate AS timestamptz))),
             date_trunc('month', timezone('Asia/Ho_Chi_Minh', CAST(:toDate AS timestamptz))),
             interval '1 month') AS m(period)
      LEFT JOIN orders o
             ON o.created_at >= (m.period AT TIME ZONE 'Asia/Ho_Chi_Minh')
            AND o.created_at < ((m.period + interval '1 month') AT TIME ZONE 'Asia/Ho_Chi_Minh')
            AND o.status = 'COMPLETED'
            AND o.is_deleted = false
      GROUP BY m.period
      ORDER BY m.period
  """, nativeQuery = true)
  List<OrderTimelineProjection> getMonthlyRevenueStatistics(
      @Param("fromDate") Instant fromDate,
      @Param("toDate") Instant toDate
  );

  /**
   * 3. Query Biểu đồ Doanh thu theo Năm (generate_series interval '1 year').
   */
  @Query(value = """
      SELECT CAST(m.period AS date) AS period,
             COALESCE(SUM(o.total_amount), 0) AS revenue,
             COUNT(o.id) AS orderCount
      FROM generate_series(
             date_trunc('year', timezone('Asia/Ho_Chi_Minh', CAST(:fromDate AS timestamptz))),
             date_trunc('year', timezone('Asia/Ho_Chi_Minh', CAST(:toDate AS timestamptz))),
             interval '1 year') AS m(period)
      LEFT JOIN orders o
             ON o.created_at >= (m.period AT TIME ZONE 'Asia/Ho_Chi_Minh')
            AND o.created_at < ((m.period + interval '1 year') AT TIME ZONE 'Asia/Ho_Chi_Minh')
            AND o.status = 'COMPLETED'
            AND o.is_deleted = false
      GROUP BY m.period
      ORDER BY m.period
  """, nativeQuery = true)
  List<OrderTimelineProjection> getYearlyRevenueStatistics(
      @Param("fromDate") Instant fromDate,
      @Param("toDate") Instant toDate
  );
}
