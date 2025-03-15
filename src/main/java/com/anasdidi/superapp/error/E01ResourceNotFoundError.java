/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.error;

public class E01ResourceNotFoundError extends BaseError {

  public E01ResourceNotFoundError() {
    this("E01", "Resource Not Found!");
  }

  E01ResourceNotFoundError(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }
}
