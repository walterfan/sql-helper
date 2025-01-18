package com.github.walterfan.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.IOUtils;

public class ConfigLoader {
  protected Properties cfgProp = new Properties();
  
  private static ConfigLoader m_instance = new ConfigLoader();
  
  public static ConfigLoader getInstance() {
    return m_instance;
  }
  
  public static ConfigLoader getInstance(String filepath) {
    try {
      m_instance.load(filepath);
      return m_instance;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } 
  }
  
  public void loadFromClassPath(String name) throws IOException {
    loadFromClassPath(name, Thread.currentThread().getContextClassLoader());
  }
  
  public void loadFromClassPath(String name, ClassLoader loader) throws IOException {
    InputStream is = null;
    try {
      is = loader.getResourceAsStream(name);
      if (is != null)
        this.cfgProp.load(is);
    } finally {
      IOUtils.closeQuietly(is);
    } 
  }
  
  public static String readFromClassPath(String name) throws IOException {
    InputStream is = null;
    try {
      is = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
      if (is != null)
        return IOUtils.toString(is); 
    } finally {
      IOUtils.closeQuietly(is);
    } 
    return null;
  }
  
  public void load(String pathName) throws IOException {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(pathName);
      this.cfgProp.load(fis);
    } finally {
      IOUtils.closeQuietly(fis);
    } 
  }
  
  public static Map<String, String> loadAsMap(String pathName) throws IOException {
    Properties prop = new Properties();
    FileInputStream fis = null;
    Map<String, String> map = new HashMap<>();
    try {
      fis = new FileInputStream(pathName);
      prop.load(fis);
      for (Map.Entry<Object, Object> entry : prop.entrySet())
        map.put("" + entry.getKey(), "" + entry.getValue()); 
    } finally {
      IOUtils.closeQuietly(fis);
    } 
    return map;
  }
  
  public synchronized void setPropConfig(Properties prop) {
    this.cfgProp = prop;
  }
  
  public Properties getProperties() {
    return this.cfgProp;
  }
  
  public String get(String key) {
    return this.cfgProp.getProperty(key);
  }
  
  public String getProperty(String key) {
    return this.cfgProp.getProperty(key);
  }
  
  public String getProperty(String key, String defaultValue) {
    return this.cfgProp.getProperty(key, defaultValue);
  }
  
  public int getIntProperty(String key, int defaultValue) {
    int val = defaultValue;
    try {
      val = Integer.parseInt(getProperty(key));
    } catch (NumberFormatException ne) {
      val = defaultValue;
    } 
    return val;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder("");
    for (Map.Entry<Object, Object> entry : this.cfgProp.entrySet())
      sb.append((new StringBuilder()).append(entry.getKey()).append("=").append(entry.getValue()).append("\n").toString()); 
    return sb.toString();
  }
}
