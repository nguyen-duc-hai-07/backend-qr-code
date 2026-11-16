package org.oplearn.project.exception;

import org.oplearn.project.exception.base.BadRequestException;

public class TableInactiveException extends BadRequestException {
  public TableInactiveException() {
    super("org.oplearn.project.exception.TableInactiveException");
  }
}
