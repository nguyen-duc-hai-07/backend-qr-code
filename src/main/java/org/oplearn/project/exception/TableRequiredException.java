package org.oplearn.project.exception;

import org.oplearn.project.exception.base.BadRequestException;

public class TableRequiredException extends BadRequestException {
  public TableRequiredException() {
    super("org.oplearn.project.exception.TableRequiredException");
  }
}
