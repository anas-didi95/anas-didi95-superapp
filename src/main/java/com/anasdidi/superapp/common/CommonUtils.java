/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common;

import io.vertx.ext.web.RoutingContext;
import java.util.UUID;

public final class CommonUtils {

  private static final String EVENTBUS_ADDR_FMT = "%s:%s";
  private static final String CTX_TRACEID = "traceId";

  public static final String prepareEventBusAddress(Class<?> clazz, String eventType) {
    return prepareEventBusAddress(clazz.getSimpleName(), eventType);
  }

  public static final String prepareEventBusAddress(String prefix, String eventType) {
    return EVENTBUS_ADDR_FMT.formatted(prefix, eventType);
  }

  public static final void setTraceId(RoutingContext ctx) {
    ctx.put(CTX_TRACEID, UUID.randomUUID().toString());
  }

  public static final String getTraceId(RoutingContext ctx) {
    return ctx.get(CTX_TRACEID);
  }
}
