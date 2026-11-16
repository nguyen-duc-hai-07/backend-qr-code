package org.oplearn.project.exception;

import org.oplearn.project.exception.base.ForbiddenException;

public class UserForbiddenException extends ForbiddenException {
  public UserForbiddenException() {
    super("org.oplearn.project.exception.UserUnauthorizedException");
  }
}
