package org.oplearn.project.exception;

import org.oplearn.project.exception.base.BadRequestException;

public class OrderStatusNotPendingException extends BadRequestException {
  public OrderStatusNotPendingException() {
    super("org.oplearn.project.exception.OrderStatusNotPendingException");
  }
}
