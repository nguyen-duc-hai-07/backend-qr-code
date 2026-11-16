package org.oplearn.project.service;

import org.oplearn.project.dto.request.CategoryRequest;
import org.oplearn.project.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

  CategoryResponse create(CategoryRequest request);

  CategoryResponse update(Long id, CategoryRequest request);

  void delete(Long id);

  CategoryResponse detail(Long id);

  List<CategoryResponse> getAllActive();

  List<CategoryResponse> getAll();

  default List<CategoryResponse> getAll(Boolean isActiveOnly) {
    return Boolean.TRUE.equals(isActiveOnly) ? getAllActive() : getAll();
  }
}
