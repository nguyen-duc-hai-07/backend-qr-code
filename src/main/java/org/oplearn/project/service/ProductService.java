package org.oplearn.project.service;

import org.oplearn.project.dto.request.ProductRequest;
import org.oplearn.project.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

  ProductResponse create(ProductRequest request);

  ProductResponse update(Long id, ProductRequest request);

  void delete(Long id);

  ProductResponse detail(Long id);

  List<ProductResponse> getAllActive();

  List<ProductResponse> getAll();

  List<ProductResponse> getByCategoryId(Long categoryId);

  List<ProductResponse> getActiveByCategoryId(Long categoryId);

  List<ProductResponse> search(String keyword, Long categoryId, Boolean onlyActive);

  ProductResponse updateAvailability(Long id, Boolean isAvailable);

  void deleteByCategoryId(Long categoryId);

  default List<ProductResponse> getAll(String keyword, Long categoryId, Boolean isAvailableOnly) {
    return search(keyword, categoryId, isAvailableOnly != null && isAvailableOnly);
  }
}
