/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth;

import com.anasdidi.superapp.common.BaseRepository;
import com.anasdidi.superapp.error.E03RecordNotFoundError;
import com.anasdidi.superapp.error.E03RecordNotFoundError.E003RecordEnum;
import com.anasdidi.superapp.verticle.auth.entity.UserEntity;
import com.anasdidi.superapp.verticle.auth.entity.UserSessionEntity;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.templates.RowMapper;
import io.vertx.sqlclient.templates.SqlTemplate;
import io.vertx.sqlclient.templates.TupleMapper;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthRepository extends BaseRepository {

  public Future<UserEntity> getUserByUsername(SqlConnection conn, String username) {
    String sql =
        """
        SELECT a.ID, a.USERNAME, a.PASSWORD, a.SALT, a.CHANNEL_ID
        FROM TBL_USER a
        WHERE a.USERNAME = #{username} AND NOT a.IS_DEL
        """;
    JsonObject params = JsonObject.of().put("username", username);
    RowMapper<UserEntity> rowMapper =
        r -> {
          UserEntity entity = new UserEntity();
          entity.setId(r.getUUID("ID"));
          entity.setUsername(r.getString("USERNAME"));
          entity.setPassword(r.getString("PASSWORD"));
          entity.setSalt(r.getString("SALT"));
          entity.setChannelId(r.getString("CHANNEL_ID"));
          return entity;
        };

    return SqlTemplate.forQuery(conn, sql)
        .mapFrom(TupleMapper.jsonObject())
        .mapTo(rowMapper)
        .execute(params)
        .map(
            o -> {
              if (o.size() > 0) {
                return o.iterator().next();
              }
              throw new E03RecordNotFoundError(E003RecordEnum.UserEntity);
            });
  }

  public Future<UserSessionEntity> getUserSessionByUserId(SqlConnection conn, UUID userId) {
    String sql =
        """
        SELECT a.ID, a.USER_ID, a.VALID_AFTER_DT
        FROM TBL_USER_SESSION a
        WHERE a.USER_ID = #{userId}
        """;
    JsonObject param = JsonObject.of().put("userId", userId);
    RowMapper<UserSessionEntity> rowMapper =
        row -> {
          UserSessionEntity entity = new UserSessionEntity();
          entity.setId(row.getUUID("ID"));
          entity.setUserId(row.getUUID("USER_ID"));
          entity.setValidAfterDate(row.getOffsetDateTime("VALID_AFTER_DT"));
          return entity;
        };

    return SqlTemplate.forQuery(conn, sql)
        .mapFrom(TupleMapper.jsonObject())
        .mapTo(rowMapper)
        .execute(param)
        .map(o -> o.size() > 0 ? o.iterator().next() : null);
  }

  public Future<Void> insertUserSession(SqlConnection conn, UUID userId) {
    String sql =
        """
        INSERT INTO TBL_USER_SESSION (ID, USER_ID, VALID_AFTER_DT)
        VALUES (#{id}, #{userId}, #{validAfterDate})
        """;
    JsonObject param =
        JsonObject.of()
            .put("id", UUID.randomUUID())
            .put("userId", userId)
            .put("validAfterDate", OffsetDateTime.now());

    return SqlTemplate.forUpdate(conn, sql)
        .mapFrom(TupleMapper.jsonObject())
        .execute(param)
        .compose(o -> Future.succeededFuture());
  }

  public Future<Void> updateUserSession(SqlConnection conn, UUID sessionId) {
    String sql =
        """
        UPDATE TBL_USER_SESSION SET
         VALID_AFTER_DT=#{validAfterDate}
        WHERE ID=#{sessionId}
        """;
    JsonObject param =
        JsonObject.of().put("sessionId", sessionId).put("validAfterDate", OffsetDateTime.now());

    return SqlTemplate.forUpdate(conn, sql)
        .mapFrom(TupleMapper.jsonObject())
        .execute(param)
        .compose(o -> Future.succeededFuture());
  }

  public Future<Void> upsertUserSession(SqlConnection conn, UUID userId) {
    return getUserSessionByUserId(conn, userId)
        .compose(
            o ->
                Optional.ofNullable(o)
                    .map(oo -> updateUserSession(conn, oo.getId()))
                    .orElse(insertUserSession(conn, userId)));
  }

  public Future<Void> insertUser(
      SqlConnection conn, String username, String hashPassword, String salt) {
    String sql =
        """
        INSERT INTO TBL_USER (ID, CREATE_BY, CREATE_DT, IS_DEL, USERNAME, PASSWORD, SALT)
        VALUES (#{id}, 'SYSTEM', #{createDate}, false, #{username}, #{hashPassword}, #{salt});
        """;
    JsonObject param =
        JsonObject.of()
            .put("id", UUID.randomUUID().toString())
            .put("createDate", OffsetDateTime.now())
            .put("username", username)
            .put("hashPassword", hashPassword)
            .put("salt", salt);

    return SqlTemplate.forUpdate(conn, sql)
        .mapFrom(TupleMapper.jsonObject())
        .execute(param)
        .compose(o -> Future.succeededFuture());
  }

  public Future<List<String>> getUserAuthorizationList(SqlConnection conn, UUID userId) {
    String sql =
        """
        SELECT d.cd || c.CD
        FROM TBL_USER a
        INNER JOIN TBL_ROLE_PERMISSION b ON b.ROLE_ID = a.ROLE_ID
        INNER JOIN TBL_PERMISSION c ON c.ID = b.PERMISSION_ID
        INNER JOIN TBL_MODULE d ON d.ID = c.MODULE_ID
        WHERE a.ID = #{userId}
          AND NOT d.IS_DEL AND NOT c.IS_DEL
        """;
    JsonObject param = JsonObject.of().put("userId", userId.toString());

    return SqlTemplate.forQuery(conn, sql)
        .mapFrom(TupleMapper.jsonObject())
        .execute(param)
        .map(
            o -> {
              RowIterator<Row> iterator = o.iterator();
              List<String> resultList = new ArrayList<>();
              while (iterator.hasNext()) {
                resultList.add(iterator.next().getString(0));
              }
              return resultList;
            });
  }
}
