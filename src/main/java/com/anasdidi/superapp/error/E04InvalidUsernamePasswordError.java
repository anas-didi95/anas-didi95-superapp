/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.error;

public class E04InvalidUsernamePasswordError extends BaseError {

  public E04InvalidUsernamePasswordError() {
    this("E04", "Invalid Username/Password!");
  }

  E04InvalidUsernamePasswordError(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }
}
