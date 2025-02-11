/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.openapi.validation.RequestParameter;
import io.vertx.openapi.validation.ValidatedRequest;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseService<A extends BaseReqDto, B extends BaseResDto> {

  private static final Logger logger = LogManager.getLogger(BaseService.class);
  private final Class<A> bodyClass;

  public BaseService(Class<A> bodyClass) {
    this.bodyClass = bodyClass;
  }

  protected abstract String getOperationId();

  protected abstract B handle(A body, JsonObject path, JsonObject query);

  public void process(RoutingContext ctx) {
    UUID traceId = ctx.get("traceId");
    String tag = "%s:%s:%s".formatted(traceId, this.getClass().getSimpleName(), getOperationId());

    long timeStart = System.currentTimeMillis();
    logger.info("[{}] START...", tag);

    ValidatedRequest validatedRequest = ctx.get(RouterBuilder.KEY_META_DATA_VALIDATED_REQUEST);
    A body = JsonObject.mapFrom(validatedRequest.getBody()).mapTo(bodyClass);
    JsonObject query = prepareQuery(validatedRequest.getQuery());
    JsonObject path = preparePath(validatedRequest.getPathParameters());

    logger.info("[{}] body={}, path={}, query={}", tag, body, path, query);
    B result = handle(body, path, query);
    logger.info("[{}] result={}", tag, result);

    logger.info("[{}] END...{}ms", tag, System.currentTimeMillis() - timeStart);
    ctx.response().end(JsonObject.mapFrom(result).encode());
  }

  protected JsonObject prepareQuery(Map<String, RequestParameter> query) {
    return JsonObject.of();
  }

  protected JsonObject preparePath(Map<String, RequestParameter> path) {
    return JsonObject.of();
  }
}
