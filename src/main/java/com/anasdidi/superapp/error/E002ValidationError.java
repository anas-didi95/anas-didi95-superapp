/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.error;

import io.vertx.openapi.validation.SchemaValidationException;

public class E002ValidationError extends BaseError {

  public E002ValidationError(SchemaValidationException e) {
    this("E002", e.getMessage());
  }

  E002ValidationError(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }
}
