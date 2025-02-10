/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.helloworld;

import com.anasdidi.superapp.helloworld.service.HelloWorldService;
import com.anasdidi.superapp.helloworld.service.impl.GreetingService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.openapi.router.OpenAPIRoute;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HelloWorldVerticle extends AbstractVerticle {

  private static final Logger logger = LogManager.getLogger(HelloWorldVerticle.class);
  private final RouterBuilder routerBuilder;
  private Map<String, HelloWorldService<?, ?>> serviceMap;

  public HelloWorldVerticle(RouterBuilder routerBuilder) {
    this.routerBuilder = routerBuilder;
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    routerBuilder
        .getRoutes()
        .forEach(o -> System.out.println("ii :: " + o.getOperation().getOperationId()));
    this.serviceMap =
        Arrays.asList(new GreetingService()).stream()
            .collect(Collectors.toMap(o -> o.getOperationId(), Function.identity()));
    this.serviceMap.forEach(
        (k, v) -> {
          logger.info("[start] Map operatinId {}", k);
          OpenAPIRoute route = routerBuilder.getRoute(k);
          route.addHandler(v::process);
        });
    startPromise.complete();
  }
}
