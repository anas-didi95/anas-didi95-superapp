/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.error;

public class E003RecordNotFoundError extends BaseError {

  public E003RecordNotFoundError(E003RecordEnum record) {
    this("E003", "Record[%s] Not Found!".formatted(record.name()));
  }

  E003RecordNotFoundError(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }

  public enum E003RecordEnum {
    UserEntity
  }
}
