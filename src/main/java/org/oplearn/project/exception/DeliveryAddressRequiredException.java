package org.oplearn.project.exception;

import org.oplearn.project.exception.base.BadRequestException;

public class DeliveryAddressRequiredException extends BadRequestException {
  public DeliveryAddressRequiredException() {
    super("org.oplearn.project.exception.DeliveryAddressRequiredException");
  }
}
