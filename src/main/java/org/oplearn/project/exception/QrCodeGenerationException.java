package org.oplearn.project.exception;

import org.oplearn.project.exception.base.InternalServerError;

public class QrCodeGenerationException extends InternalServerError {
  public QrCodeGenerationException() {
    super("org.oplearn.project.exception.QrCodeGenerationException", "Lỗi tạo hình ảnh mã QR");
  }

  public QrCodeGenerationException(String message) {
    super("org.oplearn.project.exception.QrCodeGenerationException", message);
  }
}
