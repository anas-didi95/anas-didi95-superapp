/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.helloworld;

import com.anasdidi.superapp.common.BaseService;
import com.anasdidi.superapp.common.BaseVerticle;
import com.anasdidi.superapp.verticle.helloworld.service.impl.GreetingService;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HelloWorldVerticle extends BaseVerticle {

  @Override
  protected Map<String, BaseService> setServiceMap() {
    return Arrays.asList(new GreetingService()).stream()
        .collect(Collectors.toMap(o -> o.getOperationId(), Function.identity()));
  }
}
