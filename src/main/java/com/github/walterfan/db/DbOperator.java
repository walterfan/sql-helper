package com.github.walterfan.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DbOperator implements ConnectionOperator {
  private static Log logger = LogFactory.getLog(DbOperator.class);
  
  private ConnectionHolder connHolder = null;
  
  private DbQueryRunner runner = null;
  
  public DbOperator(ConnectionHolder holder) {
    this.connHolder = holder;
    this.runner = new DbQueryRunner(holder);
  }
  
  public DbOperator(Connection conn) {
    if (conn == null)
      throw new IllegalArgumentException("Connection is null"); 
    this.connHolder = new DbConn(conn);
    this.runner = new DbQueryRunner(this.connHolder);
  }
  
  public DbOperator(ConnectionProvider provider) {
    this.connHolder = new DbConn(provider);
    this.runner = new DbQueryRunner(this.connHolder);
  }
  
  public int[] batch(String sql, Object[][] params) throws SQLException {
    return this.runner.batch(getConnection(), sql, params);
  }
  
  public Object query(String sql, ResultSetHandler rsh) throws SQLException {
    return this.runner.query(getConnection(), sql, rsh);
  }
  
  public Object query(String sql, Object param, ResultSetHandler rsh) throws SQLException {
    return this.runner.query(getConnection(), sql, param, rsh);
  }
  
  public Object query(String sql, Object[] params, ResultSetHandler rsh) throws SQLException {
    return this.runner.query(getConnection(), sql, params, rsh);
  }
  
  public int update(String sql) throws SQLException {
    return this.runner.update(getConnection(), sql);
  }
  
  public int update(String sql, Object param) throws SQLException {
    return this.runner.update(getConnection(), sql, param);
  }
  
  public int update(String sql, Object... params) throws SQLException {
    return this.runner.update(getConnection(), sql, params);
  }
  
  public void setCurStmt(Statement stmt, String sql) {
    if (this.connHolder == null)
      return; 
    this.connHolder.setCurStmt(stmt, sql);
  }
  
  public String getCurSql() {
    if (this.connHolder == null)
      return null; 
    return this.connHolder.getCurSql();
  }
  
  public void setConnection(Connection conn) {
    this.connHolder.setConnection(conn);
  }
  
  public void cancel() {
    if (this.connHolder == null)
      return; 
    this.connHolder.cancel();
  }
  
  public void closeConnection() {
    if (this.connHolder == null)
      return; 
    this.connHolder.closeConnection();
  }
  
  public void commit() throws SQLException {
    if (this.connHolder == null)
      return; 
    this.connHolder.commit();
  }
  
  public Connection getConnection() throws SQLException {
    if (this.connHolder == null)
      throw new IllegalStateException("connHolder is null"); 
    Connection conn = this.connHolder.getConnection();
    if (conn == null)
      throw new SQLException("connection is null"); 
    return conn;
  }
  
  public Connection createConnection() throws SQLException {
    if (this.connHolder == null)
      throw new SQLException("connHolder is null"); 
    return this.connHolder.createConnection();
  }
  
  public void rollback() throws SQLException {
    if (this.connHolder == null)
      return; 
    this.connHolder.rollback();
  }
  
  public boolean isClosed() {
    if (this.connHolder == null)
      return true; 
    return this.connHolder.isClosed();
  }
  
  public void setQueryTimeout(int seconds) {
    if (this.runner != null)
      this.runner.setQueryTimeout(seconds); 
  }
  
  public void setFetchSize(int fetchSize) {
    if (this.runner != null)
      this.runner.setFetchSize(fetchSize); 
  }
}
