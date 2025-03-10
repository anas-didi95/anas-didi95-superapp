/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp;

import com.anasdidi.superapp.common.BaseVerticle;
import com.anasdidi.superapp.common.CommonConstants;
import com.anasdidi.superapp.common.CommonUtils;
import com.anasdidi.superapp.error.BaseError;
import com.anasdidi.superapp.error.E000InternalServerError;
import com.anasdidi.superapp.error.E001ResourceNotFoundError;
import com.anasdidi.superapp.error.E002ValidationError;
import com.anasdidi.superapp.verticle.auth.AuthVerticle;
import com.anasdidi.superapp.verticle.helloworld.HelloWorldVerticle;
import com.anasdidi.superapp.verticle.tracelog.TraceLogVerticle;
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
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.openapi.router.RequestExtractor;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.openapi.contract.MediaType;
import io.vertx.openapi.contract.OpenAPIContract;
import io.vertx.openapi.validation.SchemaValidationException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainVerticle extends AbstractVerticle {

  private static final Logger logger = LogManager.getLogger(MainVerticle.class);

  static {
    System.setProperty("io.vertx.web.router.setup.lenient", "true");
    DatabindCodec.mapper().findAndRegisterModules();
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    long timeStart = System.currentTimeMillis();
    Future<JsonObject> config = processConfig();
    Future<Void> appConfig = config.compose(this::prepareAppConfig);
    Future<RouterBuilder> routerBuilder =
        config.flatMap(
            o -> processOpenApi(o.getJsonObject(CommonConstants.CFG_APP).getString("openApiPath")));
    Future<CompositeFuture> verticle =
        Future.all(config, routerBuilder)
            .compose(
                o ->
                    processVerticle(
                        routerBuilder.result(),
                        config.result(),
                        Arrays.asList(
                            new HelloWorldVerticle(), new TraceLogVerticle(), new AuthVerticle())));

    Future.all(config, appConfig, routerBuilder, verticle)
        .onComplete(
            o -> {
              logger.info("[start] All verticles started...{}", verticle.isComplete());

              Router mainRouter = Router.router(vertx);
              mainRouter.route().handler(BodyHandler.create());
              mainRouter
                  .route()
                  .handler(
                      ctx -> {
                        CommonUtils.setTraceId(ctx);
                        ctx.next();
                      });
              mainRouter.route("/*").subRouter(routerBuilder.result().createRouter());
              mainRouter.errorHandler(
                  500,
                  ctx -> {
                    if (ctx.failure() instanceof SchemaValidationException e) {
                      ctx.fail(400, new E002ValidationError(e));
                    } else {
                      logger.error("{} ERROR...", getTag(ctx, 500), ctx.failure());

                      E000InternalServerError e =
                          ctx.failure() instanceof E000InternalServerError
                              ? (E000InternalServerError) ctx.failure()
                              : new E000InternalServerError(ctx.failure().getMessage());
                      ctx.response()
                          .setStatusCode(500)
                          .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                          .end(e.getResponseBody(ctx).encode());
                    }
                  });
              mainRouter.errorHandler(
                  404,
                  ctx -> {
                    logger.warn("{} ERROR...", getTag(ctx, 404), ctx.failure());

                    E001ResourceNotFoundError e = new E001ResourceNotFoundError();
                    ctx.response()
                        .setStatusCode(404)
                        .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .end(e.getResponseBody(ctx).encode());
                  });
              mainRouter.errorHandler(
                  400,
                  ctx -> {
                    logger.debug("{} ERROR...", getTag(ctx, 400), ctx.failure());

                    if (ctx.failure() instanceof BaseError ee) {
                      ctx.response()
                          .setStatusCode(400)
                          .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                          .end(ee.getResponseBody(ctx).encode());
                    } else {
                      ctx.fail(500, ctx.failure());
                    }
                  });

              int port = config.result().getJsonObject(CommonConstants.CFG_APP).getInteger("port");
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
                              Objects.nonNull(oo.getJsonObject(CommonConstants.CFG_APP)));
                          logger.info("[processConfig] Get version...{}", oo.getString("version"));
                        },
                        oo -> logger.error("Fail to get app config!", oo)));
  }

  private Future<Void> prepareAppConfig(JsonObject config) {
    return Future.future(
        promise -> {
          JsonObject security =
              config.getJsonObject(CommonConstants.CFG_APP).getJsonObject("security");
          AppConfig.INSTANCE.setJwtOptions(
              new JWTOptions()
                  .setIssuer(security.getString("issuer"))
                  .setExpiresInSeconds(security.getInteger("expiresInSeconds"))
                  .setAudience(Arrays.asList(security.getString("audience").split(","))));
          AppConfig.INSTANCE.setJwtAuth(
              JWTAuth.create(
                  vertx,
                  new JWTAuthOptions()
                      .setJWTOptions(AppConfig.INSTANCE.getJwtOptions())
                      .addPubSecKey(
                          new PubSecKeyOptions()
                              .setAlgorithm("HS256")
                              .setBuffer(security.getString("secret")))));
          promise.complete();
        });
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
        (o1, o2) ->
            o1.getJsonObject(CommonConstants.CFG_APP)
                .getJsonObject("verticle")
                .getJsonObject(o2.getClass().getSimpleName());

    List<Future<String>> deployList =
        verticleList.stream()
            .filter(
                o -> {
                  boolean enabled =
                      vtxConfig.apply(appConfig, o).getBoolean(CommonConstants.CFG_ENABLED, false);
                  logger.info(
                      "[processVerticle] {} enabled...{}", o.getClass().getSimpleName(), enabled);
                  return enabled;
                })
            .map(
                o -> {
                  o.setRouterBuilder(routerBuilder);
                  return vertx.deployVerticle(
                      o,
                      new DeploymentOptions()
                          .setConfig(
                              vtxConfig
                                  .apply(appConfig, o)
                                  .put("version", appConfig.getString("version"))
                                  .put("env", appConfig.getString("APP_ENV", "prod"))));
                })
            .toList();
    return Future.all(deployList);
  }

  private String getTag(RoutingContext ctx, int statusCode) {
    String traceId = CommonUtils.getTraceId(ctx);
    return CommonUtils.getTag(traceId, this.getClass(), "StsCd[%d]".formatted(statusCode));
  }
}
