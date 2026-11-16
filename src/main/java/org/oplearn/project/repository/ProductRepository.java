package org.oplearn.project.repository;

import org.oplearn.project.entity.Product;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

  Optional<Product> findByIdAndIsDeletedFalse(Long id);

  List<Product> findAllByIsDeletedFalse();

  List<Product> findAllByIsDeletedFalse(Sort sort);

  List<Product> findAllByIsAvailableTrueAndIsDeletedFalseOrderByCreatedAtDesc();

  List<Product> findAllByCategoryIdAndIsDeletedFalse(Long categoryId);

  List<Product> findAllByCategoryIdAndIsAvailableTrueAndIsDeletedFalse(Long categoryId);

  @Query("""
    SELECT p FROM Product p
    WHERE p.isDeleted = false
      AND (
        LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR (p.description IS NOT NULL AND LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
      )
    ORDER BY p.id ASC
  """)
  List<Product> searchByKeyword(@Param("keyword") String keyword);

  @Query("""
    SELECT p FROM Product p
    WHERE p.isDeleted = false
      AND p.isAvailable = true
      AND (
        LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR (p.description IS NOT NULL AND LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
      )
    ORDER BY p.id ASC
  """)
  List<Product> searchActiveByKeyword(@Param("keyword") String keyword);

  @Query("""
    SELECT p FROM Product p
    WHERE p.isDeleted = false
      AND p.categoryId = :categoryId
      AND (
        LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR (p.description IS NOT NULL AND LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
      )
    ORDER BY p.id ASC
  """)
  List<Product> searchByKeywordAndCategory(
      @Param("keyword") String keyword,
      @Param("categoryId") Long categoryId
  );

  @Query("""
    SELECT p FROM Product p
    WHERE p.isDeleted = false
      AND p.isAvailable = true
      AND p.categoryId = :categoryId
      AND (
        LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR (p.description IS NOT NULL AND LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
      )
    ORDER BY p.id ASC
  """)
  List<Product> searchActiveByKeywordAndCategory(
      @Param("keyword") String keyword,
      @Param("categoryId") Long categoryId
  );

  boolean existsByNameAndIsDeletedFalse(String name);

  boolean existsByNameAndIdNotAndIsDeletedFalse(String name, Long id);
}
