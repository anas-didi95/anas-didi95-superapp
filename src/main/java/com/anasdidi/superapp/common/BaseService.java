/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common;

import com.anasdidi.superapp.verticle.tracelog.TraceLogVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.openapi.validation.RequestParameter;
import io.vertx.openapi.validation.ValidatedRequest;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseService<A extends BaseReqDto, B extends BaseResDto> {

  private static final Logger logger = LogManager.getLogger(BaseService.class);
  private final Class<A> bodyClass;
  protected EventBus eventBus;
  protected BaseRepository repository;

  public BaseService(Class<A> bodyClass) {
    this.bodyClass = bodyClass;
  }

  public abstract String getOperationId();

  protected abstract B handle(InboundDto<A> dto, JsonObject opts);

  protected abstract A parseMessage(JsonObject body, MultiMap headers);

  protected abstract JsonObject prepareQuery(Map<String, RequestParameter> query);

  protected abstract JsonObject preparePath(Map<String, RequestParameter> path);

  public void process(RoutingContext ctx, JsonObject opts) {
    String traceId = ctx.get("traceId");
    long timeStart = System.currentTimeMillis();
    logger.info("{} START...", getTag(traceId));

    ValidatedRequest validatedRequest = ctx.get(RouterBuilder.KEY_META_DATA_VALIDATED_REQUEST);
    A body = JsonObject.mapFrom(validatedRequest.getBody()).mapTo(bodyClass);
    JsonObject query = prepareQuery(validatedRequest.getQuery());
    JsonObject path = preparePath(validatedRequest.getPathParameters());

    InboundDto<A> in = new InboundDto<>(body, path, query);
    logger.info("{} IN :: {}", getTag(traceId), in);

    B result = handle(in, opts);
    logger.info("{} RESULT :: {}", getTag(traceId), result);
    ctx.response().end(JsonObject.mapFrom(result).encode());

    OutboundDto<B> out = new OutboundDto<>(result);
    logger.info("{} OUT :: {}", getTag(traceId), out);

    eventBus.publish(
        CommonUtils.prepareEventBusAddress(TraceLogVerticle.class, "SAVE_LOG"),
        JsonObject.of("in", JsonObject.mapFrom(in), "out", JsonObject.mapFrom(out))
            .put("opts", opts),
        new DeliveryOptions()
            .addHeader(
                "EV_ORIGIN", "%s:%s".formatted(this.getClass().getSimpleName(), getOperationId()))
            .addHeader("EV_TRACEID", traceId));
    logger.info("{} END...{}ms", getTag(traceId), System.currentTimeMillis() - timeStart);
  }

  public void process(Message<Object> msg, JsonObject opts) {
    String traceId = msg.headers().get("EV_TRACEID");
    String origin = msg.headers().get("EV_ORIGIN");
    long timeStart = System.currentTimeMillis();
    logger.info("{} START...origin={}", getTag(traceId), origin);

    A message = parseMessage((JsonObject) msg.body(), msg.headers());
    InboundDto<A> in = new InboundDto<>(message, null, null);
    logger.info("{} IN :: {}", getTag(traceId), in);

    B result = handle(in, opts);
    logger.info("{} RESULT :: {}", getTag(traceId), result);

    logger.info("{} END...{}ms", getTag(traceId), System.currentTimeMillis() - timeStart);
  }

  private String getTag(String traceId) {
    return "[%s:%s:%s]".formatted(traceId, this.getClass().getSimpleName(), getOperationId());
  }

  public final void setEventBus(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public final void setRepository(BaseRepository repository) {
    this.repository = repository;
  }

  public static record InboundDto<A>(A body, JsonObject path, JsonObject query) {}

  public static record OutboundDto<B>(B result) {}
}
