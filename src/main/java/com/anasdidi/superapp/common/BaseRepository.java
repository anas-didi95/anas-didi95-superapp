/* (C) Anas Juwaidi Bin Mohd Jeffry. All rights reserved. */
package com.anasdidi.superapp.common;

import io.vertx.jdbcclient.JDBCPool;

public abstract class BaseRepository {

  protected JDBCPool datasource;

  public final void setDatasource(JDBCPool datasource) {
    this.datasource = datasource;
  }
}
