package com.github.walterfan.db;


import java.sql.SQLException;
import java.util.Date;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.walterfan.util.ConfigLoader;

public final class ConnectionProviderFactory {
  private static Log logger = LogFactory.getLog(ConnectionProviderFactory.class);
  
  public static ConnectionProvider createConnectionProvider(String clsName, DbConfig cfg) throws SQLException {
    ConnectionProvider provider = null;
    if (StringUtils.isBlank(clsName)) {
      logger.error("Have not set provider class");
      return null;
    } 
    try {
      Class<?> cls = Class.forName(clsName);
      provider = (ConnectionProvider)cls.newInstance();
      provider.setDbConfig(cfg);
    } catch (Exception e) {
      throw new SQLException(e.getMessage());
    } 
    return provider;
  }
  
  public static ConnectionProvider createDbcpConnectionPool(DbConfig dbCfg) {
    return new DriverManagerProvider(dbCfg);
  }
  
  public static ConnectionProvider createDriverManagerProvider(DbConfig dbCfg) {
    return new DriverManagerProvider(dbCfg);
  }
  
  public static void main(String[] args) throws Exception {
    ConfigLoader cfgLoader = ConfigLoader.getInstance();
    cfgLoader.load("./conf/jdbc.properties");
    DbConfig dbCfg = new DbConfig(cfgLoader.getProperty("jdbc.driverClass"), cfgLoader.getProperty("jdbc.url"), cfgLoader.getProperty("jdbc.username"), cfgLoader.getProperty("jdbc.password"));
    ConnectionProvider provider = createDriverManagerProvider(dbCfg);
    if (provider == null)
      return; 
    DbOperator opt = null;
    try {
      opt = new DbOperator(provider);
      opt.createConnection();
      Date date = (Date)opt.query("select current_timestamp", (ResultSetHandler)new ScalarHandler(1));
      System.out.println("current date:" + date);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      opt.closeConnection();
    } 
  }
}
