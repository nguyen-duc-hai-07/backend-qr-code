package org.oplearn.project.exception;

import org.oplearn.project.exception.base.BadRequestException;

import java.util.Map;

public class ProductUnavailableException extends BadRequestException {
  public ProductUnavailableException() {
    super("org.oplearn.project.exception.ProductUnavailableException", Map.of("name", "này"));
  }

  public ProductUnavailableException(String productName) {
    super("org.oplearn.project.exception.ProductUnavailableException", Map.of("name", productName != null ? productName : "này"));
  }
}
