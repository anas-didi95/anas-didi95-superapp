/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp;

import com.anasdidi.superapp.helloworld.HelloWorldVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.openapi.router.RequestExtractor;
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
            .compose(
                o ->
                    Future.succeededFuture(
                        RouterBuilder.create(vertx, o, RequestExtractor.withBodyHandler())));

    Future<String> helloWorld =
        openApi
            .compose(o -> vertx.deployVerticle(new HelloWorldVerticle(o)))
            .onComplete(o -> System.out.println("ONCOMPLETE"))
            .onFailure(o -> System.err.println(o));

    Future.all(openApi, helloWorld)
        .onSuccess(
            o -> {
              System.out.println("Verticle " + o.result().resultAt(1));
              RouterBuilder routerBuilder = o.result().resultAt(0);
              Router router = routerBuilder.createRouter();
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

              Router mainRouter = Router.router(vertx);
              mainRouter.route("/*").subRouter(router);

              vertx
                  .createHttpServer()
                  .requestHandler(mainRouter)
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
