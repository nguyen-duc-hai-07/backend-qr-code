package org.oplearn.project.facade;

import org.oplearn.project.dto.request.ProductRequest;
import org.oplearn.project.dto.response.ProductResponse;

import java.util.List;

public interface ProductFacadeService {

  ProductResponse create(ProductRequest request);

  ProductResponse update(Long id, ProductRequest request);

  ProductResponse updateAvailability(Long id, Boolean isAvailable);

  ProductResponse detail(Long id);

  void delete(Long id);

  List<ProductResponse> getAll(String keyword, Long categoryId, Boolean isAvailableOnly);
}
