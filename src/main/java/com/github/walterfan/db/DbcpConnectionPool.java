package com.github.walterfan.db;

import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DbcpConnectionPool extends DataSourceProvider {
  public static final String PROP_DRIVERCLASSNAME = "driverClassName";
  
  public static final String PROP_PASSWORD = "password";
  
  public static final String PROP_URL = "url";
  
  public static final String PROP_USERNAME = "username";
  
  public static final String DBCP_CFG_FILE = "dbcp.properties";
  
  private static Log logger = LogFactory.getLog(DbcpConnectionPool.class);
  
  private Properties prop = new Properties();
  
  private BasicDataSource dataSource = null;
  
  public DbcpConnectionPool() {}
  
  public DbcpConnectionPool(DbConfig cfg) {
    setDbConfig(cfg);
  }
  
  public void close() {
    if (this.dataSource == null)
      return; 
    try {
      this.dataSource.close();
    } catch (Exception e) {
      logger.error(e);
    } 
  }
  
  public synchronized DataSource getDataSource() {
    return (DataSource)this.dataSource;
  }
  
  private boolean isOracle10g() {
    if ("oracle.jdbc.driver.OracleDriver".equalsIgnoreCase(getDbCfg().getDriverClass()))
      return true; 
    return false;
  }
  
  public void setDbConfig(DbConfig dbCfg) {
    super.setDbConfig(dbCfg);
    this.prop.setProperty("driverClassName", dbCfg.getDriverClass());
    this.prop.setProperty("url", dbCfg.getUrl());
    this.prop.setProperty("username", dbCfg.getUserName());
    this.prop.setProperty("password", dbCfg.getPassword());
  }
  
  public int getConnectionCount() {
    if (this.dataSource != null)
      return this.dataSource.getNumActive(); 
    return 0;
  }
  
  public DataSource createDataSource() throws SQLException {
    if (this.dataSource != null)
      return (DataSource)this.dataSource; 
    try {
      this.dataSource = (BasicDataSource)BasicDataSourceFactory.createDataSource(this.prop);
    } catch (Exception e) {
      throw new SQLException(e.getMessage());
    } 
    if (isOracle10g() && this.dataSource != null) {
      this.dataSource.addConnectionProperty("oracle.jdbc.V8Compatible", "true");
      this.dataSource.addConnectionProperty("oracle.net.CONNECT_TIMEOUT", "" + getLoginTimeoutMS());
    } 
    return (DataSource)this.dataSource;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder("DbcpConnectionPool:\n");
    for (Map.Entry<Object, Object> entry : this.prop.entrySet()) {
      sb.append((new StringBuilder()).append(entry.getKey()).append("=").append(entry.getValue()).toString());
      sb.append("\n");
    } 
    return sb.toString();
  }
}
