package org.oplearn.project.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.CategoryRequest;
import org.oplearn.project.dto.response.CategoryMenuResponse;
import org.oplearn.project.dto.response.CategoryResponse;
import org.oplearn.project.dto.response.ProductResponse;
import org.oplearn.project.facade.CategoryFacadeService;
import org.oplearn.project.service.CategoryService;
import org.oplearn.project.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryFacadeServiceImpl implements CategoryFacadeService {

  private final CategoryService categoryService;
  private final ProductService productService;

  @Override
  @Transactional
  public CategoryResponse create(CategoryRequest request) {
    log.info("(CategoryFacade) create category: {}", request.getName());
    return categoryService.create(request);
  }

  @Override
  @Transactional
  public CategoryResponse update(Long id, CategoryRequest request) {
    log.info("(CategoryFacade) update category id: {}", id);
    return categoryService.update(id, request);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    log.info("(CategoryFacade) delete category id: {}", id);
    // 1. Soft-delete danh mục
    categoryService.delete(id);
    // 2. Cascade soft-delete toàn bộ sản phẩm thuộc danh mục này
    productService.deleteByCategoryId(id);
  }

  @Override
  public CategoryResponse detail(Long id) {
    return categoryService.detail(id);
  }

  @Override
  public List<CategoryResponse> getAll(Boolean isActiveOnly) {
    return categoryService.getAll(isActiveOnly);
  }

  @Override
  public List<CategoryMenuResponse> getPublicMenu() {
    log.info("(CategoryFacade) getPublicMenu");
    // Lấy các danh mục đang hoạt động
    List<CategoryResponse> activeCategories = categoryService.getAll(true);

    // Gom nhóm kèm các món ăn có sẵn tương ứng
    return activeCategories.stream().map(cat -> {
      List<ProductResponse> products = productService.getAll(null, cat.getId(), true);
      return CategoryMenuResponse.builder()
          .id(cat.getId())
          .name(cat.getName())
          .displayOrder(cat.getDisplayOrder())
          .products(products)
          .build();
    }).toList();
  }
}
