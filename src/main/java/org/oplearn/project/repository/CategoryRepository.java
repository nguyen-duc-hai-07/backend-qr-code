package org.oplearn.project.repository;

import org.oplearn.project.entity.Category;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findByIdAndIsDeletedFalse(Long id);

  List<Category> findAllByIsDeletedFalse();

  List<Category> findAllByIsDeletedFalse(Sort sort);

  List<Category> findAllByIsDeletedFalseOrderByDisplayOrderAsc();

  List<Category> findAllByIsActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc();

  boolean existsByNameAndIsDeletedFalse(String name);

  boolean existsByNameAndIdNotAndIsDeletedFalse(String name, Long id);
}
