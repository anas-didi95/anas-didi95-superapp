/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.error;

public class E05UserSessionExpiredError extends BaseError {

  public E05UserSessionExpiredError() {
    this("E05", "User Session Expired!");
  }

  E05UserSessionExpiredError(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }
}
