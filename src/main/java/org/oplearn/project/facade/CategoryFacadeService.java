package org.oplearn.project.facade;

import org.oplearn.project.dto.request.CategoryRequest;
import org.oplearn.project.dto.response.CategoryMenuResponse;
import org.oplearn.project.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryFacadeService {

  CategoryResponse create(CategoryRequest request);

  CategoryResponse update(Long id, CategoryRequest request);

  void delete(Long id);

  CategoryResponse detail(Long id);

  List<CategoryResponse> getAll(Boolean isActiveOnly);

  List<CategoryMenuResponse> getPublicMenu();
}
