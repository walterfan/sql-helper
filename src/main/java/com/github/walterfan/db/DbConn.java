package com.github.walterfan.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.walterfan.util.ConfigLoader;

public class DbConn implements ConnectionHolder {
  private static Log logger = LogFactory.getLog("DbConn.class");
  
  private ConnectionProvider provider = null;
  
  private Connection conn = null;
  
  private Statement curStmt = null;
  
  private String curSql = null;
  
  private boolean debug = false;
  
  public DbConn(Connection conn) {
    this.conn = conn;
  }
  
  public DbConn(ConnectionProvider provider) {
    this.provider = provider;
  }
  
  public void setConnection(Connection conn) {
    this.conn = conn;
  }
  
  public Connection createConnection() throws SQLException {
    if (this.provider == null)
      throw new RuntimeException("Please set connection provider firstly"); 
    if (this.conn != null && !this.conn.isClosed())
      this.conn.close(); 
    this.conn = this.provider.getConnection();
    if (this.conn != null)
      this.conn.setAutoCommit(false); 
    return this.conn;
  }
  
  public void closeConnection() {
    DbUtils.closeQuietly(this.conn);
    this.conn = null;
  }
  
  public boolean isClosed() {
    if (this.conn == null)
      return true; 
    try {
      return this.conn.isClosed();
    } catch (SQLException e) {
      logger.error(e);
      return true;
    } 
  }
  
  public Connection getConnection() {
    return this.conn;
  }
  
  public synchronized void setCurStmt(Statement stmt, String sql) {
    this.curStmt = stmt;
    this.curSql = sql;
  }
  
  public String getCurSql() {
    return this.curSql;
  }
  
  public void commit() throws SQLException {
    if (this.conn != null)
      this.conn.commit(); 
  }
  
  public void rollback() throws SQLException {
    if (this.conn != null)
      this.conn.rollback(); 
  }
  
  public void cancel() {
    if (this.curStmt != null) {
      try {
        logger.info("Cancel " + this.curSql);
        this.curStmt.cancel();
      } catch (Exception e) {
        logger.error(e);
      } 
    } else {
      logger.debug("curStmt is null");
    } 
  }
  
  public void close() {
    if (this.conn != null)
      try {
        this.conn.close();
      } catch (Exception e) {
        logger.error(e);
      }  
  }
  
  public void execute(String sql) throws Exception {
    if (this.conn == null)
      return; 
    Statement stmt = null;
    ResultSet rs = null;
    try {
      stmt = this.conn.createStatement();
      setCurStmt(stmt, sql);
      boolean status = stmt.execute(sql);
      do {
        if (status) {
          rs = stmt.getResultSet();
          DbHelper.printResultsTable(rs, System.out);
        } else {
          int numUpdates = stmt.getUpdateCount();
          if (this.debug)
            logger.info("Ok. " + numUpdates + " rows affected."); 
        } 
        status = stmt.getMoreResults();
      } while (status || stmt.getUpdateCount() != -1);
    } finally {
      DbUtils.closeQuietly(rs);
      DbUtils.closeQuietly(stmt);
      setCurStmt(null, null);
    } 
  }
  
  public static void main(String[] args) {
    DbConfig dbCfg = null;
    if (args.length >= 3);
    ConfigLoader cfgLoader = ConfigLoader.getInstance();
    try {
      cfgLoader.loadFromClassPath("devaid.properties");
    } catch (IOException e1) {
      e1.printStackTrace();
    } 
    dbCfg = new DbConfig(cfgLoader.get("db_driverClass"), cfgLoader.get("db_url"), cfgLoader.get("db_username"), cfgLoader.get("db_password"));
    ConnectionProvider provider = new DriverManagerProvider();
    provider.setDbConfig(dbCfg);
    DbConn dc = new DbConn(provider);
    try {
      if (dc.createConnection() == null) {
        logger.info("getConnection error, please check the specified jdbc parameters");
        return;
      } 
      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      System.out.print("SQL> ");
      System.out.flush();
      String strsql = in.readLine();
      if (strsql == null || strsql.equals(""))
        strsql = "SELECT to_char(sysdate,'mm/dd/yy hh24:mi:ss') as now from dual"; 
      dc.execute(strsql);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(cfgLoader.get("db_driverClass") + "," + cfgLoader.get("db_url") + "," + cfgLoader
          .get("db_username") + "," + cfgLoader.get("db_password"));
    } finally {
      dc.closeConnection();
    } 
  }
}
