/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.openapi.contract.OpenAPIContract;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainVerticle extends AbstractVerticle {

  private static final Logger logger = LogManager.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    int port = 8888;
    String openApiContractPath = "openapi.yml";
    Future<RouterBuilder> openApi =
        OpenAPIContract.from(vertx, openApiContractPath)
            .onFailure(error -> logger.error("[start] Fail to get contract!", error))
            .onSuccess(o -> logger.info("[start] Get contract {}", openApiContractPath))
            .compose(o -> Future.succeededFuture(RouterBuilder.create(vertx, o)));

    openApi.andThen(
        o -> {
          o.result()
              .getRoutes()
              .forEach(
                  oo -> {
                    logger.info(
                        "[start] Setup OpenApi operation: {}", oo.getOperation().getOperationId());
                    oo.addHandler(
                        ctx ->
                            ctx.response()
                                .setStatusCode(200)
                                .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                                .end(JsonObject.of("name", "anas").encode()));
                  });

          Router router = o.result().createRouter();
          router.errorHandler(
              404,
              routingContext -> {
                JsonObject errorObject =
                    new JsonObject()
                        .put("code", 404)
                        .put(
                            "message",
                            (routingContext.failure() != null)
                                ? routingContext.failure().getMessage()
                                : "Not Found");
                routingContext
                    .response()
                    .setStatusCode(404)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(errorObject.encode());
              });
          router.errorHandler(
              400,
              routingContext -> {
                JsonObject errorObject =
                    new JsonObject()
                        .put("code", 400)
                        .put(
                            "message",
                            (routingContext.failure() != null)
                                ? routingContext.failure().getMessage()
                                : "Validation Exception");
                routingContext
                    .response()
                    .setStatusCode(400)
                    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .end(errorObject.encode());
              });

          vertx
              .createHttpServer()
              .requestHandler(o.result().createRouter())
              .listen(port)
              .onComplete(
                  http -> {
                    if (http.succeeded()) {
                      startPromise.complete();
                      logger.info("HTTP server started on port {}", port);
                    } else {
                      startPromise.fail(http.cause());
                    }
                  });
        });
  }
}
