package org.oplearn.project.repository;

import org.oplearn.project.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

  Optional<OrderItem> findByIdAndIsDeletedFalse(Long id);

  List<OrderItem> findByOrderIdAndIsDeletedFalse(Long orderId);
}
