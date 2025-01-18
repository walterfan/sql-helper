package com.github.walterfan.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider {
  void setDbConfig(DbConfig paramDbConfig);
  
  Connection getConnection() throws SQLException;
  
  int getConnectionCount();
  
  void close();
}
