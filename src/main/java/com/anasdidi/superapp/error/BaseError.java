/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.error;

import com.anasdidi.superapp.common.CommonConstants;
import com.anasdidi.superapp.common.CommonUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public abstract class BaseError extends RuntimeException {

  private final String errorCode;
  private final String errorMessage;

  BaseError(String errorCode, String errorMessage) {
    super(errorMessage);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  public final JsonObject getResponseBody(RoutingContext ctx) {
    return JsonObject.of()
        .put("errorCode", errorCode)
        .put("errorMessage", errorMessage)
        .put(CommonConstants.CTX_TRACEID, CommonUtils.getTraceId(ctx));
  }
}
