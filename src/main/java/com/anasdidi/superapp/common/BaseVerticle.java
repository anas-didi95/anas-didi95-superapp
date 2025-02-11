/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.openapi.router.OpenAPIRoute;
import io.vertx.ext.web.openapi.router.RouterBuilder;
import io.vertx.openapi.contract.Operation;
import java.util.Map;
import java.util.Objects;
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

  protected abstract Map<String, BaseService<?, ?>> getServiceMap();

  protected abstract String getLiquibaseLabel();

  @Override
  public final void start(Promise<Void> startPromise) throws Exception {
    long timeStart = System.currentTimeMillis();

    Future.all(processRouter(), processDatabase())
        .onComplete(
            o -> {
              logger.info(
                  "[{}:start] Verticle started...{}ms",
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
          this.serviceMap = getServiceMap();

          for (OpenAPIRoute route : this.routerBuilder.getRoutes()) {
            Operation operation = route.getOperation();
            BaseService<?, ?> service = this.serviceMap.get(operation.getOperationId());
            if (Objects.isNull(service)) {
              continue;
            }
            route.addHandler(service::process);
            logger.info(
                "[{}:start] Register route {}...{}",
                this.getClass().getSimpleName(),
                operation.getOpenAPIPath(),
                operation.getOperationId());
          }
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
                    rollback.addArgumentValue(
                        CommonArgumentNames.CHANGELOG_FILE.getArgumentName(),
                        "/db/db.changelog-main.yml");
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
                update.addArgumentValue(
                    CommonArgumentNames.CHANGELOG_FILE.getArgumentName(),
                    "/db/db.changelog-main.yml");
                update.addArgumentValue(argUrl, db.getString(argUrl));
                update.addArgumentValue(argUsername, db.getString(argUsername));
                update.addArgumentValue(argPassword, db.getString(argPassword));
                update.addArgumentValue(UpdateCommandStep.LABEL_FILTER_ARG, getLiquibaseLabel());
                update.execute();
              });
          logger.info(
              "[{}:processDatabase] Running Liquibase...{}ms",
              this.getClass().getSimpleName(),
              System.currentTimeMillis() - timeStart);
          return null;
        });
  }
}
