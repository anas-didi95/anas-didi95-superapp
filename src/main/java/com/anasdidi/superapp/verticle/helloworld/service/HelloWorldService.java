/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.helloworld.service;

import com.anasdidi.superapp.common.BaseService;
import com.anasdidi.superapp.verticle.helloworld.dto.HelloWorldBaseReqDto;
import com.anasdidi.superapp.verticle.helloworld.dto.HelloWorldBaseResDto;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.openapi.validation.RequestParameter;
import io.vertx.openapi.validation.ValidatedRequest;
import java.util.Map;

public abstract class HelloWorldService<
        A extends HelloWorldBaseReqDto, B extends HelloWorldBaseResDto>
    implements BaseService {

  private final Class<A> bodyClass;

  public HelloWorldService(Class<A> bodyClass) {
    this.bodyClass = bodyClass;
  }

  protected abstract B handle(A body, JsonObject query);

  public abstract String getOperationId();

  public void process(RoutingContext ctx) {
    ValidatedRequest validatedRequest = ctx.get(RouterBuilder.KEY_META_DATA_VALIDATED_REQUEST);

    A body = JsonObject.mapFrom(validatedRequest.getBody()).mapTo(getBodyClass());
    JsonObject query = prepareQuery(validatedRequest.getQuery());
    B result = handle(body, query);
    ctx.response().end(JsonObject.mapFrom(result).encode());
  }

  protected JsonObject prepareQuery(Map<String, RequestParameter> query) {
    return new JsonObject();
  }

  private Class<A> getBodyClass() {
    return this.bodyClass;
  }
}
