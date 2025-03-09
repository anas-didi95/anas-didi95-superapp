/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.error;

public class E001ResourceNotFoundError extends BaseError {

  public E001ResourceNotFoundError() {
    this("E001", "Resource Not Found!");
  }

  E001ResourceNotFoundError(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }
}
