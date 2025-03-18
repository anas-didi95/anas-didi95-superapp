/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.error;

public class E03RecordNotFoundError extends BaseError {

  public E03RecordNotFoundError(E003RecordEnum record) {
    this("E03", "Record[%s] Not Found!".formatted(record.name()));
  }

  E03RecordNotFoundError(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }

  public enum E003RecordEnum {
    UserEntity,
    UserSessionEntity
  }
}
