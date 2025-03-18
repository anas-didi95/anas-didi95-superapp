/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.error;

import io.vertx.openapi.validation.SchemaValidationException;

public class E02ValidationError extends BaseError {

  public E02ValidationError(SchemaValidationException e) {
    this("E02", e.getMessage());
  }

  E02ValidationError(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }
}
