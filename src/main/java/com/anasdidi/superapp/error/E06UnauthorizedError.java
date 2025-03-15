/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.error;

public class E06UnauthorizedError extends BaseError {

  public E06UnauthorizedError() {
    this("E06", "Unauthorized!");
  }

  E06UnauthorizedError(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }
}
