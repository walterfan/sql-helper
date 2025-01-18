package com.github.walterfan.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

public abstract class DataSourceProvider implements ConnectionProvider {
  private int loginTimeoutMS = 3000;
  
  private int retryTimes = 1;
  
  private int retryIntervalSec = 1;
  
  protected DbConfig dbConfig;
  
  public void close() {}
  
  public int getConnectionCount() {
    return 0;
  }
  
  public abstract DataSource getDataSource();
  
  public abstract DataSource createDataSource() throws SQLException;
  
  public Connection getConnection() throws SQLException {
    DataSource ds = getDataSource();
    if (ds == null)
      ds = createDataSource(); 
    if (ds == null)
      return null; 
    int retryTime = 0;
    Connection conn = null;
    do {
      conn = ds.getConnection();
      if (conn == null) {
        try {
          TimeUnit.SECONDS.sleep(this.retryIntervalSec);
        } catch (InterruptedException e) {
          throw new SQLException(e.getMessage());
        } 
      } else {
        conn.setAutoCommit(false);
      } 
    } while (conn == null && ++retryTime <= this.retryTimes);
    return conn;
  }
  
  public int getLoginTimeoutMS() {
    return this.loginTimeoutMS;
  }
  
  public void setLoginTimeoutMS(int loginTimeoutMS) {
    this.loginTimeoutMS = loginTimeoutMS;
  }
  
  public int getRetryTimes() {
    return this.retryTimes;
  }
  
  public void setRetryTimes(int retryTimes) {
    this.retryTimes = retryTimes;
  }
  
  public int getRetryIntervalSec() {
    return this.retryIntervalSec;
  }
  
  public void setRetryIntervalSec(int retryIntervalSec) {
    this.retryIntervalSec = retryIntervalSec;
  }
  
  public DbConfig getDbCfg() {
    return this.dbConfig;
  }
  
  public void setDbConfig(DbConfig dbCfg) {
    this.dbConfig = dbCfg;
  }
  
  public String toString() {
    return "DataSourceProvider: " + this.dbConfig.toString();
  }
}
