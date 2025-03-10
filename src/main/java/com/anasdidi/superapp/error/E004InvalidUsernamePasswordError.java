/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.error;

public class E004InvalidUsernamePasswordError extends BaseError {

  public E004InvalidUsernamePasswordError() {
    this("E004", "Invalid Username/Password!");
  }

  E004InvalidUsernamePasswordError(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }
}
