/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.verticle.tracelog;

import com.anasdidi.superapp.common.BaseRepository;
import com.anasdidi.superapp.verticle.tracelog.dto.TraceLogSaveLogReqDto;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.templates.SqlTemplate;
import io.vertx.sqlclient.templates.TupleMapper;
import java.time.OffsetDateTime;
import java.util.Optional;

public class TraceLogRepository extends BaseRepository {

  public Future<Void> saveLog(SqlConnection conn, TraceLogSaveLogReqDto dto) {
    String sql =
        """
        INSERT INTO TBL_TRACE_LOG (CREATE_BY, CREATE_DT, TRACE_ID, ORIGIN, IN_PAYLOAD, OPTS, OUT_PAYLOAD, IS_ERR)
        VALUES (#{createBy}, #{createDate}, #{traceId}, #{origin}, #{in}, #{opts}, #{out}, #{isError})
        """;
    TupleMapper<TraceLogSaveLogReqDto> mapper =
        TupleMapper.mapper(
            o ->
                JsonObject.of()
                    .put("createBy", "SYSTEM")
                    .put("createDate", OffsetDateTime.now())
                    .put("traceId", o.traceId())
                    .put("origin", o.origin())
                    .put("in", o.in().encode())
                    .put("opts", Optional.ofNullable(o.opts()).orElse(JsonObject.of()).encode())
                    .put("out", o.out().encode())
                    .put("isError", Optional.ofNullable(o.isError()).orElse(true))
                    .getMap());

    return SqlTemplate.forUpdate(conn, sql)
        .mapFrom(mapper)
        .execute(dto)
        .compose(o -> Future.succeededFuture());
  }
}
