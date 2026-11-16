package org.oplearn.project.exception;

import org.oplearn.project.exception.base.BadRequestException;

public class InvalidOrderTypeException extends BadRequestException {
  public InvalidOrderTypeException() {
    super("org.oplearn.project.exception.InvalidOrderTypeException");
  }
}
