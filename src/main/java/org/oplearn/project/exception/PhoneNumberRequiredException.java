package org.oplearn.project.exception;

import org.oplearn.project.exception.base.BadRequestException;

public class PhoneNumberRequiredException extends BadRequestException {
  public PhoneNumberRequiredException() {
    super("org.oplearn.project.exception.PhoneNumberRequiredException");
  }
}
