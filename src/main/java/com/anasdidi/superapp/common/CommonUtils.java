/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common;

import com.anasdidi.superapp.common.BaseService.InboundDto;
import com.anasdidi.superapp.common.BaseService.OutboundDto;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class CommonUtils {

  private static final String EVENTBUS_ADDR_FMT = "%s:%s";

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
    ctx.put(CommonConstants.CTX_TRACEID, UUID.randomUUID().toString());
  }

  public static final String getTraceId(RoutingContext ctx) {
    return ctx.get(CommonConstants.CTX_TRACEID);
  }

  public static final String getTag(String traceId, Class<?> clazz, String operationId) {
    return "#%s#%s#%s#".formatted(traceId, clazz.getSimpleName(), operationId);
  }

  public static final JsonObject log(InboundDto<?> o) {
    JsonObject body =
        log(Optional.ofNullable(o.body()).map(JsonObject::mapFrom).orElse(JsonObject.of()));
    JsonObject path = log(Optional.ofNullable(o.path()).orElse(JsonObject.of()));
    JsonObject query = log(Optional.ofNullable(o.query()).orElse(JsonObject.of()));
    return JsonObject.of().put("body", body).put("path", path).put("query", query);
  }

  public static final JsonObject log(OutboundDto<?> o) {
    JsonObject result =
        log(Optional.ofNullable(o.result()).map(JsonObject::mapFrom).orElse(JsonObject.of()));
    return JsonObject.of().put("result", result);
  }

  public static final JsonObject log(JsonObject o) {
    JsonObject oo = o.copy();
    for (String k : oo.fieldNames()) {
      if (k.toLowerCase().contains("pass")) {
        Object v = oo.getValue(k);
        oo.put(k, Objects.nonNull(v) ? "*****" : null);
      }
    }
    return oo;
  }
}
