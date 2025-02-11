/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.openapi.router.OpenAPIRoute;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.openapi.contract.Operation;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseVerticle extends AbstractVerticle {

  private static final Logger logger = LogManager.getLogger(BaseVerticle.class);
  protected RouterBuilder routerBuilder;
  protected Map<String, BaseService<?, ?>> serviceMap;

  protected abstract Map<String, BaseService<?, ?>> setServiceMap();

  @Override
  public final void start(Promise<Void> startPromise) throws Exception {
    long timeStart = System.currentTimeMillis();

    this.serviceMap = setServiceMap();
    for (OpenAPIRoute route : this.routerBuilder.getRoutes()) {
      Operation operation = route.getOperation();
      BaseService<?, ?> service = this.serviceMap.get(operation.getOperationId());
      if (Objects.isNull(service)) {
        continue;
      }
      route.addHandler(service::process);
      logger.info(
          "[{}] Register route {}...{}",
          this.getClass().getSimpleName(),
          operation.getOpenAPIPath(),
          operation.getOperationId());
    }

    logger.info(
        "[{}] Verticle started...{}ms",
        this.getClass().getSimpleName(),
        System.currentTimeMillis() - timeStart);
    startPromise.complete();
  }

  public final void setRouterBuilder(RouterBuilder routerBuilder) {
    this.routerBuilder = routerBuilder;
  }
}
