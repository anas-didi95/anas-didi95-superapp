/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainVerticle extends AbstractVerticle {

  private static final Logger logger = LogManager.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    int port = 8888;

    vertx
        .createHttpServer()
        .requestHandler(
            req -> {
              req.response().putHeader("content-type", "text/plain").end("Hello from Vert.x!");
            })
        .listen(8888)
        .onComplete(
            http -> {
              if (http.succeeded()) {
                startPromise.complete();
                logger.info("HTTP server started on port {}", port);
              } else {
                startPromise.fail(http.cause());
              }
            });
  }
}
