package org.oplearn.project.exception;

import org.oplearn.project.exception.base.InternalServerError;

public class FileUploadException extends InternalServerError {
  public FileUploadException() {
    super("org.oplearn.project.exception.FileUploadException", "Không thể tải lên tệp tin");
  }

  public FileUploadException(String detail) {
    super("org.oplearn.project.exception.FileUploadException", "Không thể tải lên tệp tin: " + detail);
  }
}
