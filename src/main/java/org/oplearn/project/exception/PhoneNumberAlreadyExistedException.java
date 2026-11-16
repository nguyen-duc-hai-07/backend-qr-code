package org.oplearn.project.exception;

import org.oplearn.project.exception.base.ConflictException;

public class PhoneNumberAlreadyExistedException extends ConflictException {
  public PhoneNumberAlreadyExistedException() {
    super("org.oplearn.project.exception.PhoneNumberAlreadyExistedException");
  }
}
