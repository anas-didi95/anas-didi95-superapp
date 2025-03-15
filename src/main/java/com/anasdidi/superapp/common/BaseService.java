/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common;

import com.anasdidi.superapp.error.BaseError;
import com.anasdidi.superapp.error.E00InternalServerError;
import com.anasdidi.superapp.error.E06UnauthorizedError;
import com.anasdidi.superapp.verticle.tracelog.TraceLogVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.openapi.validation.RequestParameter;
import io.vertx.openapi.validation.ValidatedRequest;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseService<
    A extends BaseReqDto, B extends BaseResDto, C extends BaseRepository> {

  private static final Logger logger = LogManager.getLogger(BaseService.class);
  private final Class<A> bodyClass;
  private final Class<C> repoClass;
  protected Vertx vertx;
  private BaseRepository repository;

  public BaseService(Class<A> bodyClass, Class<C> repoClass) {
    this.bodyClass = bodyClass;
    this.repoClass = repoClass;
  }

  public abstract String getOperationId();

  protected abstract Future<OutboundDto<B>> handle(
      User user, InboundDto<A> dto, Map<String, Object> opts, String traceId);

  protected abstract A parseMessage(JsonObject body, MultiMap headers);

  protected abstract JsonObject prepareQuery(Map<String, RequestParameter> query);

  protected abstract JsonObject preparePath(Map<String, RequestParameter> path);

  public void process(RoutingContext ctx, Map<String, Object> opts) {
    String traceId = CommonUtils.getTraceId(ctx);
    long timeStart = System.currentTimeMillis();
    logger.info("{} START...", getTag(traceId));

    ValidatedRequest validatedRequest = ctx.get(RouterBuilder.KEY_META_DATA_VALIDATED_REQUEST);
    logger.trace("{} validatedRequest.body={}", getTag(traceId), validatedRequest.getBody());
    logger.trace(
        "{} validatedRequest.path={}", getTag(traceId), validatedRequest.getPathParameters());
    logger.trace("{} validatedRequest.query={}", getTag(traceId), validatedRequest.getQuery());

    A body =
        Optional.ofNullable(validatedRequest.getBody())
            .map(o -> o.getJsonObject())
            .map(o -> o.mapTo(bodyClass))
            .orElse(null);
    JsonObject path = preparePath(validatedRequest.getPathParameters());
    JsonObject query = prepareQuery(validatedRequest.getQuery());

    InboundDto<A> in = new InboundDto<>(body, path, query);
    logger.info("{} IN :: {}", getTag(traceId), CommonUtils.log(in));

    User user = ctx.user();
    Future<Void> permission =
        Future.future(
            promise -> {
              if (Objects.nonNull(user)) {
                boolean isAuthorized =
                    PermissionBasedAuthorization.create("*").match(user)
                        || PermissionBasedAuthorization.create(getOperationId()).match(user);
                if (!isAuthorized) {
                  promise.fail(new E06UnauthorizedError());
                }
              }
              promise.complete();
            });
    Future<OutboundDto<B>> out = permission.compose(o -> handle(user, in, opts, traceId));
    Future.all(permission, out)
        .andThen(
            o -> {
              boolean trace =
                  Optional.ofNullable(opts.get("trace"))
                      .map(String::valueOf)
                      .map(Boolean::parseBoolean)
                      .orElse(true);
              if (trace && o.succeeded()) {
                CommonUtils.prepareEBRequest(
                    vertx.eventBus(),
                    TraceLogVerticle.class,
                    "SAVE_LOG",
                    "%s:%s".formatted(this.getClass().getSimpleName(), getOperationId()),
                    traceId,
                    null,
                    JsonObject.of()
                        .put(CommonConstants.DTO_IN, CommonUtils.log(in))
                        .put(CommonConstants.DTO_OUT, CommonUtils.log(out.result()))
                        .put(CommonConstants.DTO_OPTS, opts)
                        .put(CommonConstants.DTO_ISERROR, false));
              } else if (trace && o.failed()) {
                JsonObject err;
                if (o.cause() instanceof BaseError e) {
                  err = e.getResponseBody(ctx);
                } else {
                  err = new E00InternalServerError(o.cause().getMessage()).getResponseBody(ctx);
                }

                CommonUtils.prepareEBRequest(
                    vertx.eventBus(),
                    TraceLogVerticle.class,
                    "SAVE_LOG",
                    "%s:%s".formatted(this.getClass().getSimpleName(), getOperationId()),
                    traceId,
                    null,
                    JsonObject.of()
                        .put(CommonConstants.DTO_IN, CommonUtils.log(in))
                        .put(CommonConstants.DTO_OUT, err)
                        .put(CommonConstants.DTO_OPTS, opts)
                        .put(CommonConstants.DTO_ISERROR, true));
              }
            });

    out.onComplete(
            o -> {
              logger.info("{} OUT :: {}", getTag(traceId), CommonUtils.log(o));
              ctx.response().end(JsonObject.mapFrom(o.result()).encode());
            },
            e -> {
              logger.debug("{} ERR :: {}", getTag(traceId), e.getMessage());
              if (e instanceof E00InternalServerError ee) {
                ctx.fail(500, ee);
              } else if (e instanceof BaseError ee) {
                ctx.fail(400, ee);
              } else {
                ctx.fail(500, new E00InternalServerError(e.getMessage()));
              }
            })
        .eventually(
            () -> {
              logger.info("{} END...{}ms", getTag(traceId), System.currentTimeMillis() - timeStart);
              return Future.succeededFuture();
            });
  }

  public void process(Message<Object> msg, Map<String, Object> opts) {
    String traceId = msg.headers().get(CommonConstants.EB_HEADER_TRACEID);
    String origin = msg.headers().get(CommonConstants.EB_HEADER_ORIGIN);
    User user =
        Optional.ofNullable(msg.headers().get(CommonConstants.EB_HEADER_PRINCIPAL))
            .map(JsonObject::new)
            .map(User::create)
            .orElse(null);
    long timeStart = System.currentTimeMillis();
    logger.info("{} START...origin={}", getTag(traceId), origin);

    A message = parseMessage((JsonObject) msg.body(), msg.headers());
    InboundDto<A> in = new InboundDto<>(message, null, null);
    logger.info("{} IN :: {}", getTag(traceId), CommonUtils.log(in));

    Future<OutboundDto<B>> out = handle(user, in, opts, traceId);

    out.onComplete(
            o -> {
              logger.info("{} OUT :: {}", getTag(traceId), CommonUtils.log(o));
              msg.reply(JsonObject.mapFrom(o));
            },
            e -> {
              logger.debug("{} ERR :: {}", getTag(traceId), e.getMessage());
              msg.reply(e);
            })
        .eventually(
            () -> {
              logger.info("{} END...{}ms", getTag(traceId), System.currentTimeMillis() - timeStart);
              return Future.succeededFuture();
            });
  }

  private String getTag(String traceId) {
    return CommonUtils.getTag(traceId, this.getClass(), getOperationId());
  }

  public final void setVertx(Vertx vertx) {
    this.vertx = vertx;
  }

  public final Vertx getVertx() {
    return this.vertx;
  }

  public final void setRepository(BaseRepository repository) {
    this.repository = repository;
  }

  public final C getRepository() {
    return Optional.ofNullable(repoClass).map(o -> o.cast(this.repository)).orElseThrow();
  }

  public static record InboundDto<A>(A body, JsonObject path, JsonObject query) {}

  public static record OutboundDto<B>(B result) {}
}
