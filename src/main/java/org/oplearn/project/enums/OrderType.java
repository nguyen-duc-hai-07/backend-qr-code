package org.oplearn.project.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OrderType {
  AT_TABLE,
  DELIVERY;

  @JsonCreator
  public static OrderType fromString(String value) {
    if (value == null) {
      return null;
    }
    for (OrderType type : values()) {
      if (type.name().equalsIgnoreCase(value.trim())) {
        return type;
      }
    }
    if ("DINE_IN".equalsIgnoreCase(value.trim()) || "TABLE".equalsIgnoreCase(value.trim())) {
      return AT_TABLE;
    }
    return AT_TABLE;
  }
}
