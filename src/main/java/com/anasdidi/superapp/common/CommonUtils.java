/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common;

import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CommonUtils {

  private static final String EVENTBUS_ADDR_FMT = "%s:%s";
  private static final String CTX_TRACEID = "traceId";

  public static final String prepareEBAddress(Class<?> clazz, String eventType) {
    return prepareEBAddress(clazz.getSimpleName(), eventType);
  }

  private static final String prepareEBAddress(String prefix, String eventType) {
    return EVENTBUS_ADDR_FMT.formatted(prefix, eventType);
  }

  private static final DeliveryOptions prepareEBDeliveryOptions(
      String origin, String traceId, Map<String, String> header) {
    DeliveryOptions options =
        new DeliveryOptions()
            .addHeader(CommonConstants.EB_HEADER_ORIGIN, origin)
            .addHeader(CommonConstants.EB_HEADER_TRACEID, traceId);
    Optional.ofNullable(header)
        .ifPresent(
            o ->
                o.entrySet().stream().forEach(o1 -> options.addHeader(o1.getKey(), o1.getValue())));
    return options;
  }

  public static final Future<Message<Object>> prepareEBRequest(
      EventBus eb,
      Class<?> target,
      String eventType,
      String origin,
      String traceId,
      Map<String, String> header,
      JsonObject message) {
    String address = prepareEBAddress(target, eventType);
    DeliveryOptions deliveryOptions = prepareEBDeliveryOptions(origin, traceId, header);
    return eb.request(address, message, deliveryOptions);
  }

  public static final void setTraceId(RoutingContext ctx) {
    ctx.put(CTX_TRACEID, UUID.randomUUID().toString());
  }

  public static final String getTraceId(RoutingContext ctx) {
    return ctx.get(CTX_TRACEID);
  }
}
