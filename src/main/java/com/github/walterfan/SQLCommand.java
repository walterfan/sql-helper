package com.github.walterfan;

public class SQLCommand implements Comparable<SQLCommand> {
  String name;
  
  String sql;
  
  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getSql() {
    return this.sql;
  }
  
  public void setSql(String sql) {
    this.sql = sql;
  }
  
  public String toString() {
    return "name=" + this.name + ", sql=" + this.sql;
  }
  
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = 31 * result + ((this.name == null) ? 0 : this.name.hashCode());
    result = 31 * result + ((this.sql == null) ? 0 : this.sql.hashCode());
    return result;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    SQLCommand other = (SQLCommand)obj;
    if (this.name == null) {
      if (other.name != null)
        return false; 
    } else if (!this.name.equals(other.name)) {
      return false;
    } 
    if (this.sql == null) {
      if (other.sql != null)
        return false; 
    } else if (!this.sql.equals(other.sql)) {
      return false;
    } 
    return true;
  }
  
  public int compareTo(SQLCommand arg0) {
    return this.name.compareTo(arg0.getName());
  }
}
