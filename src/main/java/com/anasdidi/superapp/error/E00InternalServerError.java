/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.error;

public class E00InternalServerError extends BaseError {

  public E00InternalServerError() {
    this("Internal Server Error!");
  }

  public E00InternalServerError(String errorMessage) {
    super("E00", errorMessage);
  }

  E00InternalServerError(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }
}
