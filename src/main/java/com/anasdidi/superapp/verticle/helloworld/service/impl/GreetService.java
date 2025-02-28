/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.helloworld.service.impl;

import com.anasdidi.superapp.verticle.helloworld.dto.HelloWorldGreetReqDto;
import com.anasdidi.superapp.verticle.helloworld.dto.HelloWorldGreetResDto;
import com.anasdidi.superapp.verticle.helloworld.service.HelloWorldService;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.openapi.validation.RequestParameter;
import java.util.Map;

public class GreetService extends HelloWorldService<HelloWorldGreetReqDto, HelloWorldGreetResDto> {

  public GreetService() {
    super(HelloWorldGreetReqDto.class);
  }

  @Override
  protected Future<OutboundDto<HelloWorldGreetResDto>> handle(
      InboundDto<HelloWorldGreetReqDto> dto, Map<String, Object> opts) {
    String lang = dto.query().getString("lang", "eng");
    String value =
        switch (lang) {
          case "eng" -> "Welcome.";
          case "mly" -> "Selamat datang.";
          default -> "Welcome.";
        };
    return Future.succeededFuture(new OutboundDto<>(new HelloWorldGreetResDto(lang, value), false));
  }

  @Override
  protected JsonObject prepareQuery(Map<String, RequestParameter> query) {
    return JsonObject.of("lang", query.get("lang").getString("eng"));
  }

  @Override
  public String getOperationId() {
    return "greet";
  }

  @Override
  protected HelloWorldGreetReqDto parseMessage(JsonObject body, MultiMap headers) {
    throw new UnsupportedOperationException("Unimplemented method 'parseMessage'");
  }

  @Override
  protected JsonObject preparePath(Map<String, RequestParameter> path) {
    return JsonObject.of();
  }
}
