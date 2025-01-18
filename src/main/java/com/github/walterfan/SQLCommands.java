package com.github.walterfan;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class SQLCommands implements Iterable<Map.Entry<String, SQLCommand>> {
  String type;
  
  Map<String, SQLCommand> sqlMap = new HashMap<>();
  
  public String getType() {
    return this.type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public Map<String, SQLCommand> getSqlMap() {
    return this.sqlMap;
  }
  
  public SQLCommand getSQLCommand(String name) {
    return this.sqlMap.get(name);
  }
  
  public void addSQLCommand(SQLCommand s) {
    this.sqlMap.put(s.getName(), s);
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder("type=" + this.type);
    for (SQLCommand cmd : this.sqlMap.values())
      sb.append(cmd + "\n"); 
    return sb.toString();
  }
  
  public Iterator<Map.Entry<String, SQLCommand>> iterator() {
    return this.sqlMap.entrySet().iterator();
  }
}
