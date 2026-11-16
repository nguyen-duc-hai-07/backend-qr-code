package org.oplearn.project.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.ProductRequest;
import org.oplearn.project.dto.response.ProductResponse;
import org.oplearn.project.facade.ProductFacadeService;
import org.oplearn.project.service.CategoryService;
import org.oplearn.project.service.ProductService;
import org.oplearn.project.websocket.EchoWebSocketHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductFacadeServiceImpl implements ProductFacadeService {

  private final ProductService productService;
  private final CategoryService categoryService;
  private final EchoWebSocketHandler echoWebSocketHandler;

  @Override
  @Transactional
  public ProductResponse create(ProductRequest request) {
    log.info("(ProductFacade) create product: {}", request.getName());

    // Xác thực danh mục tồn tại thông qua CategoryService
    categoryService.detail(request.getCategoryId());

    return productService.create(request);
  }

  @Override
  @Transactional
  public ProductResponse update(Long id, ProductRequest request) {
    log.info("(ProductFacade) update product id: {}", id);

    // Xác thực danh mục mới nếu có cập nhật categoryId
    if (request.getCategoryId() != null) {
      categoryService.detail(request.getCategoryId());
    }

    return productService.update(id, request);
  }

  @Override
  @Transactional
  public ProductResponse updateAvailability(Long id, Boolean isAvailable) {
    log.info("(ProductFacade) update availability id: {}, isAvailable: {}", id, isAvailable);

    ProductResponse updated = productService.updateAvailability(id, isAvailable);

    // Bắn WebSocket Real-time action = "PRODUCT_AVAILABILITY_CHANGED"
    echoWebSocketHandler.broadcastProductAvailability(updated);

    return updated;
  }

  @Override
  public ProductResponse detail(Long id) {
    log.info("(ProductFacade) detail id: {}", id);
    return productService.detail(id);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    log.info("(ProductFacade) delete id: {}", id);
    productService.delete(id);
  }

  @Override
  public List<ProductResponse> getAll(String keyword, Long categoryId, Boolean isAvailableOnly) {
    return productService.getAll(keyword, categoryId, isAvailableOnly);
  }
}
