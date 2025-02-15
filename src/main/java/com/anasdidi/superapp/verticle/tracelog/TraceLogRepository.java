/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.tracelog;

import com.anasdidi.superapp.common.BaseRepository;
import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogSaveLogReqDto;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import java.time.OffsetDateTime;
import java.util.Optional;

public class TraceLogRepository extends BaseRepository {

  public Future<Void> saveLog(SqlConnection conn, TraceLogSaveLogReqDto dto) {
    String sql =
        "INSERT INTO TBL_TRACE_LOG (CREATE_BY, CREATE_DT, TRACE_ID, ORIGIN, IN_PAYLOAD, OPTS, OUT_PAYLOAD, IS_ERR) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    return conn.preparedQuery(sql)
        .execute(
            Tuple.of(
                "SYSTEM",
                OffsetDateTime.now(),
                dto.traceId(),
                dto.origin(),
                dto.in().encode(),
                Optional.ofNullable(dto.opts()).orElse(JsonObject.of()).encode(),
                dto.out().encode(),
                false))
        .compose(o -> Future.succeededFuture());
  }
}
