package com.github.walterfan.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DbQueryRunner extends QueryRunner {
  private static Log logger = LogFactory.getLog(DbQueryRunner.class);
  
  private ConnectionHolder holder = null;
  
  private int queryTimeout = 0;
  
  private int fetchSize = 0;
  
  public DbQueryRunner() {}
  
  public DbQueryRunner(DataSource ds) {
    super(ds);
  }
  
  public DbQueryRunner(ConnectionHolder holder) {
    this.holder = holder;
  }
  
  protected PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
    PreparedStatement stmt = conn.prepareStatement(sql);
    if (this.holder != null) {
      this.holder.setCurStmt(stmt, sql);
      logger.debug("current sql is " + sql);
    } 
    if (this.queryTimeout > 0)
      stmt.setQueryTimeout(this.queryTimeout); 
    if (this.fetchSize > 0)
      stmt.setFetchSize(this.fetchSize); 
    return stmt;
  }
  
  protected void close(Statement stmt) throws SQLException {
    if (this.holder != null) {
      logger.debug("close sql " + this.holder.getCurSql());
      this.holder.setCurStmt(null, null);
    } 
    DbUtils.close(stmt);
  }
  
  public int getQueryTimeout() {
    return this.queryTimeout;
  }
  
  public void setQueryTimeout(int queryTimeout) {
    this.queryTimeout = queryTimeout;
  }
  
  public int getFetchSize() {
    return this.fetchSize;
  }
  
  public void setFetchSize(int fetchSize) {
    this.fetchSize = fetchSize;
  }
}
