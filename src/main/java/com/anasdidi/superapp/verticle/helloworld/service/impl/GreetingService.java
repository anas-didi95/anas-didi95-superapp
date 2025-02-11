/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.helloworld.service.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.openapi.validation.RequestParameter;
import java.util.Map;

import com.anasdidi.superapp.verticle.helloworld.dto.HelloWorldGreetingReqDto;
import com.anasdidi.superapp.verticle.helloworld.dto.HelloWorldGreetingResDto;
import com.anasdidi.superapp.verticle.helloworld.service.HelloWorldService;

public class GreetingService
    extends HelloWorldService<HelloWorldGreetingReqDto, HelloWorldGreetingResDto> {

  public GreetingService() {
    super(HelloWorldGreetingReqDto.class);
  }

  public GreetingService(Class<HelloWorldGreetingReqDto> bodyClass) {
    super(bodyClass);
  }

  @Override
  protected HelloWorldGreetingResDto handle(HelloWorldGreetingReqDto body, JsonObject query) {
    String lang = query.getString("lang", "eng");
    String value =
        switch (lang) {
          case "eng" -> "Welcome.";
          case "mly" -> "Selamat datang.";
          default -> "Welcome.";
        };
    return new HelloWorldGreetingResDto(lang, value);
  }

  @Override
  public String getOperationId() {
    return "greeting";
  }

  @Override
  protected JsonObject prepareQuery(Map<String, RequestParameter> query) {
    return JsonObject.of("lang", query.get("lang").getString("eng"));
  }
}
