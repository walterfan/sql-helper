package com.github.walterfan;

import com.github.walterfan.db.DbConfig;

public class JdbcConfig extends DbConfig {
  private String name;
  
  private String type;
  
  public String getName() {
    return this.name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getType() {
    return this.type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = 31 * result + ((this.name == null) ? 0 : this.name.hashCode());
    return result;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    JdbcConfig other = (JdbcConfig)obj;
    if (this.name == null) {
      if (other.name != null)
        return false; 
    } else if (!this.name.equals(other.name)) {
      return false;
    } 
    return true;
  }
  
  public String toString() {
    return super.toString() + ", name = " + this.name + ", type=" + this.type;
  }
}
