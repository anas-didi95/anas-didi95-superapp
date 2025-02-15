/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common;

import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.SqlConnection;

public abstract class BaseRepository {

  private JDBCPool datasource;

  public final void setDatasource(JDBCPool datasource) {
    this.datasource = datasource;
  }

  public final Future<SqlConnection> getConnection() {
    return this.datasource.getConnection();
  }
}
