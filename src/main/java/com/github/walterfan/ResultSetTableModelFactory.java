package com.github.walterfan;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.table.TableModel;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.github.walterfan.db.DbConfig;

public class ResultSetTableModelFactory {
  Connection conn;
  
  DbConfig dbCfg = null;
  
  public ResultSetTableModelFactory() {}
  
  public ResultSetTableModelFactory(DbConfig dbCfg) throws ClassNotFoundException {
    setDbCfg(dbCfg);
  }
  
  public ResultSetTableModelFactory(String driverClassName, String url, String username, String password) throws ClassNotFoundException, SQLException {
    this(new DbConfig(driverClassName, url, username, password));
  }
  
  public TableModel executeSqlBlock(String sqlBlock) throws SQLException {
    checkConnection();
    CallableStatement cstmt = this.conn.prepareCall(sqlBlock);
    boolean ret = cstmt.execute(sqlBlock);
    return new KeyValueTableModel("executed", Boolean.valueOf(ret));
  }
  
  public TableModel execute4TableModel(String query) throws SQLException {
    checkConnection();
    String sql = StringUtils.lowerCase(query);
    if (sql.startsWith("commit")) {
      this.conn.commit();
      return new KeyValueTableModel("Commited", "Successful");
    } 
    if (sql.startsWith("rollback")) {
      this.conn.rollback();
      return new KeyValueTableModel("Rollbacked", "Successful");
    } 
    if (StringUtils.contains(this.dbCfg.getDriverClass(), "org.sqlite.JDBC")) {
      Statement statement = this.conn.createStatement();
      boolean bool = statement.execute(query);
      if (bool) {
        ResultSet resultSet = statement.getResultSet();
        return new ResultTableModel(resultSet);
      } 
      int i = statement.getUpdateCount();
      return new KeyValueTableModel("UpdateCount", Integer.valueOf(i));
    } 
    Statement stmt = this.conn.createStatement(1004, 1007);
    ResultSet rs = null;
    boolean status = stmt.execute(query);
    if (status) {
      rs = stmt.getResultSet();
      return new ResultSetTableModel(rs);
    } 
    int cnt = stmt.getUpdateCount();
    return new KeyValueTableModel("UpdateCount", Integer.valueOf(cnt));
  }
  
  private void checkConnection() throws SQLException, IllegalStateException {
    if (this.conn == null || this.conn.isClosed())
      this.conn = DriverManager.getConnection(this.dbCfg.getUrl(), this.dbCfg
          .getUserName(), this.dbCfg.getPassword()); 
    if (this.conn == null)
      throw new IllegalStateException("Connection cannot be established."); 
    this.conn.setAutoCommit(false);
  }
  
  public void setDbCfg(DbConfig dbCfg) throws ClassNotFoundException {
    this.dbCfg = dbCfg;
    Class<?> driver = Class.forName(dbCfg.getDriverClass());
  }
  
  public void close() {
    DbUtils.closeQuietly(this.conn);
    this.conn = null;
  }
  
  protected void finalize() {
    close();
  }
  
  public DbConfig getDbCfg() {
    return this.dbCfg;
  }
}
