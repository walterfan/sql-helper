package com.github.walterfan.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public interface ConnectionHolder {
  Connection getConnection() throws SQLException;
  
  Connection createConnection() throws SQLException;
  
  boolean isClosed();
  
  void closeConnection();
  
  void setConnection(Connection paramConnection);
  
  void cancel();
  
  void setCurStmt(Statement paramStatement, String paramString);
  
  String getCurSql();
  
  void commit() throws SQLException;
  
  void rollback() throws SQLException;
}
