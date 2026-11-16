package org.oplearn.project.exception;

import org.oplearn.project.exception.base.ConflictException;

public class StoreTableAlreadyExistedException extends ConflictException {
  public StoreTableAlreadyExistedException() {
    super("org.oplearn.project.exception.StoreTableAlreadyExistedException");
  }
}
