/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp;

import com.anasdidi.superapp.common.BaseVerticle;
import com.anasdidi.superapp.verticle.helloworld.HelloWorldVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.openapi.router.RequestExtractor;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.openapi.contract.OpenAPIContract;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.exception.CommandExecutionException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainVerticle extends AbstractVerticle {

  private static final Logger logger = LogManager.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    long timeStart = System.currentTimeMillis();
    Future<JsonObject> config = processConfig();
    Future<RouterBuilder> routerBuilder =
        config.flatMap(o -> processOpenApi(o.getJsonObject("app").getString("openApiPath")));
    Future<Void> database =
        config.compose(
            o -> processDatabase(o.getString("version"), o.getString("APP_ENV", "prod")));
    Future<CompositeFuture> verticle =
        Future.all(config, routerBuilder)
            .compose(
                o ->
                    processVerticle(
                        routerBuilder.result(),
                        config.result().getJsonObject("app").getJsonObject("verticle"),
                        Arrays.asList(new HelloWorldVerticle())));

    Future.all(config, routerBuilder, database, verticle)
        .onComplete(
            o -> {
              logger.info("[start] All verticles started...{}", verticle.isComplete());
              Router router = routerBuilder.result().createRouter();
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
              mainRouter
                  .route()
                  .handler(
                      ctx -> {
                        UUID traceId = UUID.randomUUID();
                        ctx.put("traceId", traceId);
                        ctx.next();
                      });
              mainRouter.route("/*").subRouter(router);

              int port = config.result().getJsonObject("app").getInteger("port");
              vertx
                  .createHttpServer()
                  .requestHandler(mainRouter)
                  .listen(port)
                  .onComplete(
                      oo -> {
                        logger.info(
                            "[start] HTTP server started on port {}...{}ms",
                            port,
                            System.currentTimeMillis() - timeStart);
                        startPromise.complete();
                      },
                      startPromise::fail);
            },
            startPromise::fail);
  }

  private Future<JsonObject> processConfig() {
    Function<JsonObject, String> configPath = o -> o.getString("config.path", "application.yml");

    return ConfigRetriever.create(
            vertx, new ConfigRetrieverOptions().addStore(new ConfigStoreOptions().setType("sys")))
        .getConfig()
        .onComplete(
            o -> logger.info("[processConfig] Get config path...{}", configPath.apply(o)),
            o -> logger.error("[processConfig] Fail to get config path!", o))
        .compose(
            o ->
                ConfigRetriever.create(
                        vertx,
                        new ConfigRetrieverOptions()
                            .addStore(
                                new ConfigStoreOptions()
                                    .setOptional(false)
                                    .setType("file")
                                    .setFormat("yaml")
                                    .setConfig(JsonObject.of("path", configPath.apply(o))))
                            .addStore(
                                new ConfigStoreOptions()
                                    .setOptional(false)
                                    .setType("file")
                                    .setFormat("properties")
                                    .setConfig(JsonObject.of("path", "version.properties")))
                            .addStore(new ConfigStoreOptions().setOptional(true).setType("env")))
                    .getConfig()
                    .onComplete(
                        oo -> {
                          logger.info(
                              "[processConfig] Get app config...{}",
                              Objects.nonNull(oo.getJsonObject("app")));
                          logger.info("[processConfig] Get version...{}", oo.getString("version"));
                        },
                        oo -> logger.error("Fail to get app config!", oo)));
  }

  private Future<RouterBuilder> processOpenApi(String path) {
    return OpenAPIContract.from(vertx, path)
        .onComplete(
            o -> logger.info("[processOpenApi] Get openapi contract...{}", path),
            o -> logger.error("[processOpenApi] Fail to get openapi contract!", o))
        .compose(
            o ->
                Future.succeededFuture(
                    RouterBuilder.create(vertx, o, RequestExtractor.withBodyHandler())))
        .onComplete(
            oo -> logger.info("[processOpenApi] Build router builder...{}", Objects.nonNull(oo)),
            oo -> logger.error("[processOpenApi] Fail to build router builder!", oo));
  }

  private CompositeFuture processVerticle(
      RouterBuilder routerBuilder, JsonObject appConfig, List<BaseVerticle> verticleList) {
    BiFunction<JsonObject, BaseVerticle, JsonObject> vtxConfig =
        (o1, o2) -> o1.getJsonObject(o2.getClass().getSimpleName());

    List<Future<String>> deployList =
        verticleList.stream()
            .filter(
                o -> {
                  boolean enabled = vtxConfig.apply(appConfig, o).getBoolean("enabled", false);
                  logger.info(
                      "[processVerticle] {} enabled...{}", o.getClass().getSimpleName(), enabled);
                  return enabled;
                })
            .map(
                o -> {
                  o.setRouterBuilder(routerBuilder);
                  return vertx.deployVerticle(
                      o, new DeploymentOptions().setConfig(vtxConfig.apply(appConfig, o)));
                })
            .toList();
    return Future.all(deployList);
  }

  private Future<Void> processDatabase(String version, String env) {
    return vertx.executeBlocking(
        () -> {
          long timeStart = System.currentTimeMillis();
          logger.info("[processDatabase] Liquibase env...{}", env);
          Scope.child(
              Scope.Attr.resourceAccessor,
              new ClassLoaderResourceAccessor(),
              () -> {
                if (env.equals("dev")) {
                  try {
                    logger.info("[processDatabase] Liquibase rollback tag...{}", version);
                    CommandScope rollback = new CommandScope("rollback");
                    rollback.addArgumentValue("changelogFile", "/db/db.changelog-main.yml");
                    rollback.addArgumentValue(
                        "url", "jdbc:h2:./.h2/edumgmt;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=9090");
                    rollback.addArgumentValue("username", "sa");
                    rollback.addArgumentValue("password", "sa");
                    rollback.addArgumentValue("tag", version);
                    rollback.execute();
                  } catch (CommandExecutionException ex) {
                    logger.warn("[processDatabase] Rollback skipped!", ex);
                  }
                }

                logger.info("[processDatabase] Liquibase update...");
                CommandScope update = new CommandScope("update");
                update.addArgumentValue("changelogFile", "/db/db.changelog-main.yml");
                update.addArgumentValue(
                    "url", "jdbc:h2:./.h2/edumgmt;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=9090");
                update.addArgumentValue("username", "sa");
                update.addArgumentValue("password", "sa");
                update.execute();
              });
          logger.info(
              "[processDatabase] Running Liquibase...{}ms", System.currentTimeMillis() - timeStart);
          return null;
        });
  }
}
