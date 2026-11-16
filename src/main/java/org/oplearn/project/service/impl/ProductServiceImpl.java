package org.oplearn.project.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.ProductRequest;
import org.oplearn.project.dto.response.ProductResponse;
import org.oplearn.project.entity.Product;
import org.oplearn.project.exception.CategoryNotFoundException;
import org.oplearn.project.exception.ProductAlreadyExistedException;
import org.oplearn.project.exception.ProductNotFoundException;
import org.oplearn.project.repository.CategoryRepository;
import org.oplearn.project.repository.ProductRepository;
import org.oplearn.project.service.ProductService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  @Transactional
  public ProductResponse create(ProductRequest request) {
    log.info("(create) product: {}", request.getName());

    String name = request.getName().trim();
    if (productRepository.existsByNameAndIsDeletedFalse(name)) {
      throw new ProductAlreadyExistedException();
    }

    if (request.getCategoryId() != null) {
      categoryRepository.findByIdAndIsDeletedFalse(request.getCategoryId())
          .orElseThrow(CategoryNotFoundException::new);
    }

    String toppingsJson = "[]";
    if (request.getToppings() != null && !request.getToppings().isEmpty()) {
      try {
        toppingsJson = OBJECT_MAPPER.writeValueAsString(request.getToppings());
      } catch (Exception ignored) {
      }
    }

    Product product = Product.builder()
        .categoryId(request.getCategoryId())
        .name(name)
        .description(request.getDescription())
        .price(request.getPrice())
        .imageUrl(request.getImageUrl())
        .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : Boolean.TRUE)
        .toppings(toppingsJson)
        .build();

    Product saved = productRepository.save(product);
    return ProductResponse.from(saved);
  }

  @Override
  @Transactional
  public ProductResponse update(Long id, ProductRequest request) {
    log.info("(update) product id: {}", id);

    Product existing = productRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(ProductNotFoundException::new);

    String name = request.getName().trim();
    if (!existing.getName().equalsIgnoreCase(name)
        && productRepository.existsByNameAndIdNotAndIsDeletedFalse(name, id)) {
      throw new ProductAlreadyExistedException();
    }

    if (request.getCategoryId() != null) {
      categoryRepository.findByIdAndIsDeletedFalse(request.getCategoryId())
          .orElseThrow(CategoryNotFoundException::new);
    }

    existing.setCategoryId(request.getCategoryId());
    existing.setName(name);
    existing.setDescription(request.getDescription());
    existing.setPrice(request.getPrice());
    existing.setImageUrl(request.getImageUrl());
    if (request.getIsAvailable() != null) {
      existing.setIsAvailable(request.getIsAvailable());
    }
    if (request.getToppings() != null) {
      try {
        existing.setToppings(OBJECT_MAPPER.writeValueAsString(request.getToppings()));
      } catch (Exception ignored) {
      }
    }

    Product updated = productRepository.save(existing);
    return ProductResponse.from(updated);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    log.info("(delete) product id: {}", id);

    Product existing = productRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(ProductNotFoundException::new);

    existing.setIsDeleted(Boolean.TRUE);
    productRepository.save(existing);
  }

  @Override
  public ProductResponse detail(Long id) {
    Product existing = productRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(ProductNotFoundException::new);

    return ProductResponse.from(existing);
  }

  @Override
  public List<ProductResponse> getAllActive() {
    return productRepository.findAllByIsAvailableTrueAndIsDeletedFalseOrderByCreatedAtDesc().stream()
        .map(product -> ProductResponse.from(product))
        .toList();
  }

  @Override
  public List<ProductResponse> getAll() {
    return productRepository.findAllByIsDeletedFalse(
        Sort.by(Sort.Direction.DESC, "createdAt")
    ).stream()
        .map(product -> ProductResponse.from(product))
        .toList();
  }

  @Override
  public List<ProductResponse> getByCategoryId(Long categoryId) {
    return productRepository.findAllByCategoryIdAndIsDeletedFalse(categoryId).stream()
        .map(product -> ProductResponse.from(product))
        .toList();
  }

  @Override
  public List<ProductResponse> getActiveByCategoryId(Long categoryId) {
    return productRepository.findAllByCategoryIdAndIsAvailableTrueAndIsDeletedFalse(categoryId).stream()
        .map(product -> ProductResponse.from(product))
        .toList();
  }

  @Override
  public List<ProductResponse> search(String keyword, Long categoryId, Boolean onlyActive) {
    log.info("(search) keyword: {}, categoryId: {}, onlyActive: {}", keyword, categoryId, onlyActive);
    boolean activeOnly = !Boolean.FALSE.equals(onlyActive);
    String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

    List<Product> list;
    if (cleanKeyword != null) {
      if (categoryId != null) {
        list = activeOnly
            ? productRepository.searchActiveByKeywordAndCategory(cleanKeyword, categoryId)
            : productRepository.searchByKeywordAndCategory(cleanKeyword, categoryId);
      } else {
        list = activeOnly
            ? productRepository.searchActiveByKeyword(cleanKeyword)
            : productRepository.searchByKeyword(cleanKeyword);
      }
    } else {
      if (categoryId != null) {
        list = activeOnly
            ? productRepository.findAllByCategoryIdAndIsAvailableTrueAndIsDeletedFalse(categoryId)
            : productRepository.findAllByCategoryIdAndIsDeletedFalse(categoryId);
      } else {
        list = activeOnly
            ? productRepository.findAllByIsAvailableTrueAndIsDeletedFalseOrderByCreatedAtDesc()
            : productRepository.findAllByIsDeletedFalse(Sort.by(Sort.Direction.DESC, "createdAt"));
      }
    }

    return list.stream()
        .map(ProductResponse::from)
        .toList();
  }

  @Override
  @Transactional
  public ProductResponse updateAvailability(Long id, Boolean isAvailable) {
    log.info("(updateAvailability) id: {}, isAvailable: {}", id, isAvailable);
    Product existing = productRepository.findByIdAndIsDeletedFalse(id)
        .orElseThrow(ProductNotFoundException::new);

    existing.setIsAvailable(isAvailable);
    Product updated = productRepository.save(existing);
    return ProductResponse.from(updated);
  }

  @Override
  @Transactional
  public void deleteByCategoryId(Long categoryId) {
    log.info("(deleteByCategoryId) categoryId: {}", categoryId);
    List<Product> products = productRepository.findAllByCategoryIdAndIsDeletedFalse(categoryId);
    for (Product p : products) {
      p.setIsDeleted(true);
    }
    productRepository.saveAll(products);
  }
}
