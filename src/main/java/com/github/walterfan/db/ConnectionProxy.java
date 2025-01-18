package com.github.walterfan.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class ConnectionProxy implements InvocationHandler {
  private Object obj = null;
  
  private static int connNum = 0;
  
  private Log logger = LogFactory.getLog(ConnectionProxy.class);
  
  public ConnectionProxy(Object obj) {
    this.obj = obj;
    if (obj instanceof java.sql.Connection && obj != null)
      connNum++; 
  }
  
  public static int getConnNum() {
    return connNum;
  }
  
  public static void setConnNum(int num) {
    connNum = num;
  }
  
  public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
    Object result = null;
    try {
      result = m.invoke(this.obj, args);
      if (this.obj instanceof java.sql.Connection && m.getName().equals("close"))
        connNum--; 
      this.logger.debug("invoke method " + m.getName());
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    } catch (Exception e) {
      throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
    } 
    return result;
  }
}
