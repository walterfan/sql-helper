package com.github.walterfan.db;


import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DriverManagerProvider implements ConnectionProvider {
  private static boolean initiated = false;
  
  private DbConfig dbConfig;
  
  public DriverManagerProvider() {}
  
  public DriverManagerProvider(DbConfig dbCfg) {
    this.dbConfig = dbCfg;
  }
  
  public void close() {}
  
  public Connection getConnection() throws SQLException {
    try {
      initiate();
    } catch (Exception e) {
      throw new SQLException(e.getMessage());
    } 
    Connection conn = DriverManager.getConnection(this.dbConfig.getUrl(), this.dbConfig
        .getUserName(), this.dbConfig.getPassword());
    if (conn != null) {
      conn.setAutoCommit(false);
      conn = (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[] { Connection.class }, new ConnectionProxy(conn));
    } 
    return conn;
  }
  
  public int getConnectionCount() {
    return ConnectionProxy.getConnNum();
  }
  
  private synchronized void initiate() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    if (!initiated)
      Class.forName(this.dbConfig.getDriverClass()).newInstance(); 
  }
  
  public void setDbConfig(DbConfig dbCfg) {
    this.dbConfig = dbCfg;
  }
  
  public String toString() {
    return this.dbConfig.toString();
  }
}
