package com.github.walterfan.db;

import com.github.walterfan.util.StringUtil;

public class DbConfig {
  private String driverClass = null;
  
  private String url = null;
  
  private String userName = null;
  
  private String password = null;
  
  public DbConfig() {}
  
  public DbConfig(String drv, String url, String user, String pwd) {
    this.driverClass = drv;
    this.url = url;
    this.userName = user;
    this.password = pwd;
  }
  
  public DbConfig(String url, String user, String pwd) {
    this.url = url;
    this.userName = user;
    this.password = pwd;
  }
  
  public String getDriverClass() {
    return this.driverClass;
  }
  
  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
  }
  
  public String getUrl() {
    return this.url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getUserName() {
    return this.userName;
  }
  
  public void setUserName(String userName) {
    this.userName = userName;
  }
  
  public String getPassword() {
    return this.password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public String toString() {
    return "driverClass=" + this.driverClass + ", url=" + this.url + ", userName=" + this.userName + ", password=" + this.password;
  }
  
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = 31 * result + ((this.driverClass == null) ? 0 : this.driverClass.hashCode());
    result = 31 * result + ((this.password == null) ? 0 : this.password.hashCode());
    result = 31 * result + ((this.url == null) ? 0 : this.url.hashCode());
    result = 31 * result + ((this.userName == null) ? 0 : this.userName.hashCode());
    return result;
  }
  
  public boolean equals(Object obj) {
    if (this == obj)
      return true; 
    if (obj == null)
      return false; 
    if (getClass() != obj.getClass())
      return false; 
    DbConfig other = (DbConfig)obj;
    if (this.driverClass == null) {
      if (other.driverClass != null)
        return false; 
    } else if (!this.driverClass.equals(other.driverClass)) {
      return false;
    } 
    if (this.password == null) {
      if (other.password != null)
        return false; 
    } else if (!this.password.equals(other.password)) {
      return false;
    } 
    if (this.url == null) {
      if (other.url != null)
        return false; 
    } else if (!this.url.equals(other.url)) {
      return false;
    } 
    if (this.userName == null) {
      if (other.userName != null)
        return false; 
    } else if (!this.userName.equals(other.userName)) {
      return false;
    } 
    return true;
  }

  public void resolveEnvironmentVariables() {
    this.url = StringUtil.replacePlaceholders(this.url);
    this.userName = StringUtil.replacePlaceholders(this.userName);
    this.password = StringUtil.replacePlaceholders(this.password);
  }

}
