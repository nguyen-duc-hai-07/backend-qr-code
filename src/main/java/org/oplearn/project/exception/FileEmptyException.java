package org.oplearn.project.exception;

import org.oplearn.project.exception.base.BadRequestException;

public class FileEmptyException extends BadRequestException {
  public FileEmptyException() {
    super("org.oplearn.project.exception.FileEmptyException");
  }
}
