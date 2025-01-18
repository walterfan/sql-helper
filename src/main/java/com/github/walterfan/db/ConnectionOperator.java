package com.github.walterfan.db;

import java.sql.SQLException;
import org.apache.commons.dbutils.ResultSetHandler;

public interface ConnectionOperator extends ConnectionHolder {
  Object query(String paramString, ResultSetHandler paramResultSetHandler) throws SQLException;
  
  Object query(String paramString, Object paramObject, ResultSetHandler paramResultSetHandler) throws SQLException;
  
  Object query(String paramString, Object[] paramArrayOfObject, ResultSetHandler paramResultSetHandler) throws SQLException;
  
  int update(String paramString) throws SQLException;
  
  int update(String paramString, Object paramObject) throws SQLException;
  
  int update(String paramString, Object... paramVarArgs) throws SQLException;
  
  int[] batch(String paramString, Object[][] paramArrayOfObject) throws SQLException;
  
  void setQueryTimeout(int paramInt);
}
