package com.github.walterfan;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;

public class JdbcConfigFactory {
  private static String configFilename = "JDBCConfig.xml";
  
  private List<JdbcConfig> configList = new ArrayList<>(5);
  
  public void add(JdbcConfig jc) {
    this.configList.add(jc);
  }
  
  public JdbcConfig get(String name) {
    for (JdbcConfig cfg : this.configList) {
      if (cfg.getName().equals(name))
        return cfg; 
    } 
    return null;
  }
  
  public static String getConfigFilename() {
    return configFilename;
  }
  
  public static void setConfigFilename(String configFilename) {
    JdbcConfigFactory.configFilename = configFilename;
  }
  
  public List<JdbcConfig> getConfigList() {
    return this.configList;
  }
  
  public void setConfigList(List<JdbcConfig> configList) {
    this.configList = configList;
  }
  
  public void serialize(OutputStream os) throws IOException {
    XStream xstream = createXStream();
    xstream.toXML(this, os);
  }
  
  public void serialize() throws IOException {
    XStream xstream = createXStream();
    OutputStream fos = null;
    try {
      fos = new FileOutputStream(configFilename);
      xstream.toXML(this, fos);
    } finally {
      IOUtils.closeQuietly(fos);
    } 
  }
  
  public static JdbcConfigFactory deserialize(String xml) throws IOException {
    XStream xstream = createXStream();
    InputStream fis = null;
    try {
      fis = JdbcConfigFactory.class.getClassLoader().getResourceAsStream(xml);
      return (JdbcConfigFactory)xstream.fromXML(fis);
    } finally {
      IOUtils.closeQuietly(fis);
    } 
  }
  
  public static JdbcConfigFactory deserialize() throws IOException {
    return deserialize(configFilename);
  }
  
  private static XStream createXStream() {
    XStream xstream = new XStream((HierarchicalStreamDriver)new DomDriver());
    xstream.useAttributeFor(JdbcConfig.class, "name");
    xstream.useAttributeFor(JdbcConfig.class, "type");
    xstream.addImplicitCollection(JdbcConfigFactory.class, "configList");
    xstream.alias("JdbcConfigs", JdbcConfigFactory.class);
    xstream.alias("JdbcConfig", JdbcConfig.class);
    return xstream;
  }
  
  public static JdbcConfigFactory createJdbcConfigFactory() {
    JdbcConfigFactory factory = new JdbcConfigFactory();
    return factory;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder("");
    for (JdbcConfig cfg : this.configList)
      sb.append(cfg + "\n"); 
    return sb.toString();
  }
  
  public static void main(String[] args) throws IOException {
    JdbcConfigFactory factory = createJdbcConfigFactory();
    System.out.println(factory);
    factory.serialize();
  }
}
