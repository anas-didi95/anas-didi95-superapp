/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.auth;

import com.anasdidi.superapp.common.BaseRepository;
import com.anasdidi.superapp.error.E03RecordNotFoundError;
import com.anasdidi.superapp.error.E03RecordNotFoundError.E003RecordEnum;
import com.anasdidi.superapp.verticle.auth.entity.UserEntity;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.templates.RowMapper;
import io.vertx.sqlclient.templates.SqlTemplate;
import io.vertx.sqlclient.templates.TupleMapper;

public class AuthRepository extends BaseRepository {

  public Future<UserEntity> getUserByUsername(SqlConnection conn, String username) {
    String sql =
        """
        SELECT a.ID, a.USERNAME, a.PASSWORD
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
}
