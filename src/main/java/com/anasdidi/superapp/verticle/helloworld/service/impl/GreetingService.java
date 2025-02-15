/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.helloworld.service.impl;

import com.anasdidi.superapp.verticle.helloworld.dto.HelloWorldGreetingReqDto;
import com.anasdidi.superapp.verticle.helloworld.dto.HelloWorldGreetingResDto;
import com.anasdidi.superapp.verticle.helloworld.service.HelloWorldService;
import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.openapi.validation.RequestParameter;
import java.util.Map;

public class GreetingService
    extends HelloWorldService<HelloWorldGreetingReqDto, HelloWorldGreetingResDto> {

  public GreetingService() {
    super(HelloWorldGreetingReqDto.class);
  }

  @Override
  protected HelloWorldGreetingResDto handle(
      InboundDto<HelloWorldGreetingReqDto> dto, JsonObject opts) {
    String lang = dto.query().getString("lang", "eng");
    String value =
        switch (lang) {
          case "eng" -> "Welcome.";
          case "mly" -> "Selamat datang.";
          default -> "Welcome.";
        };
    return new HelloWorldGreetingResDto(lang, value);
  }

  @Override
  protected JsonObject prepareQuery(Map<String, RequestParameter> query) {
    return JsonObject.of("lang", query.get("lang").getString("eng"));
  }

  @Override
  public String getOperationId() {
    return "greeting";
  }

  @Override
  protected HelloWorldGreetingReqDto parseMessage(JsonObject body, MultiMap headers) {
    throw new UnsupportedOperationException("Unimplemented method 'parseMessage'");
  }

  @Override
  protected JsonObject preparePath(Map<String, RequestParameter> path) {
    return JsonObject.of();
  }
}
