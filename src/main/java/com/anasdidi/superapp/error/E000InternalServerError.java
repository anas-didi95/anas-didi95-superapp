/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.error;

public class E000InternalServerError extends BaseError {

  public E000InternalServerError() {
    this("Internal Server Error!");
  }

  public E000InternalServerError(String errorMessage) {
    super("E000", errorMessage);
  }

  E000InternalServerError(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }
}
