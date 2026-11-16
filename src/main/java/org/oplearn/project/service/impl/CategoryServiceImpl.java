package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.CategoryRequest;
import org.oplearn.project.dto.response.CategoryResponse;
import org.oplearn.project.entity.Category;
import org.oplearn.project.exception.CategoryAlreadyExistedException;
import org.oplearn.project.exception.CategoryNotFoundException;
import org.oplearn.project.repository.CategoryRepository;
import org.oplearn.project.service.CategoryService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final CategoryRepository categoryRepository;

  @Override
  @Transactional
  public CategoryResponse create(CategoryRequest request) {
    log.info("(create) category: {}", request.getName());

    String name = request.getName().trim();
    if (categoryRepository.existsByNameAndIsDeletedFalse(name)) {
      throw new CategoryAlreadyExistedException();
    }

    Category category = Category.builder()
        .name(name)
        .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
        .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE)
        .build();

    Category saved = categoryRepository.save(category);
    return CategoryResponse.from(saved);
  }

  @Override
  @Transactional
  public CategoryResponse update(Long id, CategoryRequest request) {
    log.info("(update) category id: {}", id);

    Category existing = categoryRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(CategoryNotFoundException::new);

    String name = request.getName().trim();
    if (!existing.getName().equalsIgnoreCase(name)
        && categoryRepository.existsByNameAndIdNotAndIsDeletedFalse(name, id)) {
      throw new CategoryAlreadyExistedException();
    }

    existing.setName(name);
    if (request.getDisplayOrder() != null) {
      existing.setDisplayOrder(request.getDisplayOrder());
    }
    if (request.getIsActive() != null) {
      existing.setIsActive(request.getIsActive());
    }

    Category updated = categoryRepository.save(existing);
    return CategoryResponse.from(updated);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    log.info("(delete) category id: {}", id);

    Category existing = categoryRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(CategoryNotFoundException::new);

    existing.setIsDeleted(Boolean.TRUE);
    categoryRepository.save(existing);
  }

  @Override
  public CategoryResponse detail(Long id) {
    Category existing = categoryRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(CategoryNotFoundException::new);

    return CategoryResponse.from(existing);
  }

  @Override
  public List<CategoryResponse> getAllActive() {
    return categoryRepository.findAllByIsActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc().stream()
        .map(cat -> CategoryResponse.from(cat))
        .toList();
  }

  @Override
  public List<CategoryResponse> getAll() {
    return categoryRepository.findAllByIsDeletedFalse(
        Sort.by(Sort.Direction.ASC, "displayOrder")
    ).stream()
        .map(cat -> CategoryResponse.from(cat))
        .toList();
  }
}
