package org.oplearn.project.exception;

import org.oplearn.project.exception.base.BadRequestException;

public class OrderItemsEmptyException extends BadRequestException {
  public OrderItemsEmptyException() {
    super("org.oplearn.project.exception.OrderItemsEmptyException");
  }
}
