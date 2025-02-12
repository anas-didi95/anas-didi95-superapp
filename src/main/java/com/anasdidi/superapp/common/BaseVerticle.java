/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.openapi.router.OpenAPIRoute;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.CommonArgumentNames;
import liquibase.command.core.RollbackCommandStep;
import liquibase.command.core.UpdateCommandStep;
import liquibase.exception.CommandExecutionException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseVerticle extends AbstractVerticle {

  private static final Logger logger = LogManager.getLogger(BaseVerticle.class);
  private RouterBuilder routerBuilder;
  private Map<String, BaseService<?, ?>> serviceMap;
  private Map<String, Object> handlerMap;

  protected abstract Map<String, BaseService<?, ?>> getServiceMap();

  protected abstract List<String> getLiquibaseLabel();

  @Override
  public final void start(Promise<Void> startPromise) throws Exception {
    long timeStart = System.currentTimeMillis();
    this.serviceMap = getServiceMap();
    this.handlerMap = config().getJsonObject("handler").getMap();

    Future.all(processDatabase(), processRouter(), processEventBus())
        .onComplete(
            o -> {
              logger.info(
                  "[{}:start] Verticle ready...{}ms",
                  this.getClass().getSimpleName(),
                  System.currentTimeMillis() - timeStart);
              startPromise.complete();
            },
            startPromise::fail);
  }

  public final void setRouterBuilder(RouterBuilder routerBuilder) {
    this.routerBuilder = routerBuilder;
  }

  private Future<Void> processRouter() {
    return Future.future(
        promise -> {
          long timeStart = System.currentTimeMillis();

          for (Map.Entry<String, Object> handler : this.handlerMap.entrySet()) {
            String key = handler.getKey();
            JsonObject value = (JsonObject) handler.getValue();

            if (!value.getBoolean("enabled", false)) {
              logger.warn(
                  "[{}:processRouter] Handler not enabled...{}",
                  this.getClass().getSimpleName(),
                  key);
              continue;
            }

            Optional<OpenAPIRoute> route = Optional.ofNullable(this.routerBuilder.getRoute(key));
            if (route.isEmpty()) {
              logger.warn(
                  "[{}:processRouter] Route not found...{}", this.getClass().getSimpleName(), key);
              continue;
            }

            Optional<BaseService<?, ?>> service = Optional.ofNullable(this.serviceMap.get(key));
            if (service.isEmpty()) {
              logger.warn(
                  "[{}:processRouter] Service not found...{}",
                  this.getClass().getSimpleName(),
                  key);
              continue;
            }

            route.get().addHandler(ctx -> service.get().process(ctx));
            logger.info(
                "[{}:processRouter] Register route {}...{}",
                this.getClass().getSimpleName(),
                route.get().getOperation().getOpenAPIPath(),
                key);
          }

          logger.info(
              "[{}:processRouter] Router ready...{}ms",
              this.getClass().getSimpleName(),
              System.currentTimeMillis() - timeStart);
          promise.complete();
        });
  }

  private Future<Void> processEventBus() {
    return Future.future(
        promise -> {
          long timeStart = System.currentTimeMillis();

          for (Map.Entry<String, Object> handler : this.handlerMap.entrySet()) {
            String key = handler.getKey();
            JsonObject value = (JsonObject) handler.getValue();

            if (!value.getBoolean("enabled", false)) {
              logger.warn(
                  "[{}, processEventBus] Handler not enabled...{}",
                  this.getClass().getSimpleName(),
                  key);
              continue;
            }

            Optional<String> eventType = Optional.ofNullable(value.getString("eventType"));
            if (eventType.isEmpty()) {
              logger.warn(
                  "[{}:processEventBus] Event type not defined...{}",
                  this.getClass().getSimpleName(),
                  key);
              continue;
            }

            Optional<BaseService<?, ?>> service = Optional.ofNullable(this.serviceMap.get(key));
            if (service.isEmpty()) {
              logger.warn(
                  "[{}:processRouter] Service not found...{}",
                  this.getClass().getSimpleName(),
                  key);
              continue;
            }

            String address = "%s:%s".formatted(this.getClass().getSimpleName(), eventType.get());
            vertx.eventBus().consumer(address).handler(msg -> service.get().process(msg));
            logger.info(
                "[{}:processEventBus] Register event bus {}...{}",
                this.getClass().getSimpleName(),
                address,
                key);
          }

          logger.info(
              "[{}:processEventBus] Event bus ready...{}ms",
              this.getClass().getSimpleName(),
              System.currentTimeMillis() - timeStart);
          promise.complete();
        });
  }

  private Future<Void> processDatabase() {
    return vertx.executeBlocking(
        () -> {
          long timeStart = System.currentTimeMillis();
          String version = config().getString("version");
          String env = config().getString("env");
          JsonObject db = config().getJsonObject("db");
          String labels = String.join(",", getLiquibaseLabel());
          String argChangelog = CommonArgumentNames.CHANGELOG_FILE.getArgumentName();
          String argUrl = CommonArgumentNames.URL.getArgumentName();
          String argUsername = CommonArgumentNames.USERNAME.getArgumentName();
          String argPassword = CommonArgumentNames.PASSWORD.getArgumentName();

          logger.info(
              "[{}:processDatabase] Liquibase env...{}", this.getClass().getSimpleName(), env);
          Scope.child(
              Scope.Attr.resourceAccessor,
              new ClassLoaderResourceAccessor(),
              () -> {
                if (env.equals("dev")) {
                  try {
                    logger.info(
                        "[{}:processDatabase] Liquibase rollback tag...{}",
                        this.getClass().getSimpleName(),
                        version);
                    CommandScope rollback = new CommandScope("rollback");
                    rollback.addArgumentValue(argChangelog, "/db/db.changelog-main.yml");
                    rollback.addArgumentValue(argUrl, db.getString(argUrl));
                    rollback.addArgumentValue(argUsername, db.getString(argUsername));
                    rollback.addArgumentValue(argPassword, db.getString(argPassword));
                    rollback.addArgumentValue(RollbackCommandStep.TAG_ARG, version);
                    rollback.execute();
                  } catch (CommandExecutionException ex) {
                    logger.warn(
                        "[{}:processDatabase] Rollback skipped!", this.getClass().getSimpleName());
                    logger.warn("", ex);
                  }
                }

                logger.info(
                    "[{}:processDatabase] Liquibase update...{}",
                    this.getClass().getSimpleName(),
                    getLiquibaseLabel());
                CommandScope update = new CommandScope("update");
                update.addArgumentValue(argChangelog, "/db/db.changelog-main.yml");
                update.addArgumentValue(argUrl, db.getString(argUrl));
                update.addArgumentValue(argUsername, db.getString(argUsername));
                update.addArgumentValue(argPassword, db.getString(argPassword));
                update.addArgumentValue(UpdateCommandStep.LABEL_FILTER_ARG, labels);
                update.execute();
              });
          logger.info(
              "[{}:processDatabase] Liquibase ready...{}ms",
              this.getClass().getSimpleName(),
              System.currentTimeMillis() - timeStart);
          return null;
        });
  }
}
