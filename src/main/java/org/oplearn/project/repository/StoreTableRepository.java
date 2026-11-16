package org.oplearn.project.repository;

import org.oplearn.project.entity.StoreTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreTableRepository extends JpaRepository<StoreTable, Long> {

  Optional<StoreTable> findByIdAndIsDeletedFalse(Long id);

  Optional<StoreTable> findByQrTokenAndIsDeletedFalse(String qrToken);

  List<StoreTable> findAllByIsDeletedFalse();

  List<StoreTable> findByZoneAndIsDeletedFalse(String zone);

  boolean existsByQrTokenAndIsDeletedFalse(String qrToken);

  boolean existsByLocationNameAndIsDeletedFalse(String locationName);

  boolean existsByLocationNameAndIdNotAndIsDeletedFalse(String locationName, Long id);
}
