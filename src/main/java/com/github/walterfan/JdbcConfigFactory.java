package com.github.walterfan;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class JdbcConfigFactory {
  private static String configFilename = "jdbc_config.yaml";
  
  private List<JdbcConfig> configs = new ArrayList<>(5);
  
  public void add(JdbcConfig jc) {
    this.configs.add(jc);
  }
  
  public JdbcConfig get(String name) {
    for (JdbcConfig cfg : this.configs) {
      if (cfg.getName().equals(name))
        return cfg; 
    } 
    return null;
  }

  public static void setConfigFilename(String configFilename) {
    JdbcConfigFactory.configFilename = configFilename;
  }
  
  public List<JdbcConfig> getConfigs() {
    return this.configs;
  }
  
  public void setConfigs(List<JdbcConfig> configs) {
    this.configs = configs;
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



  public static JdbcConfigFactory deserialize(String yamlFile) throws IOException {
    InputStream fis = null;
    try {
      fis = JdbcConfigFactory.class.getClassLoader().getResourceAsStream(yamlFile);
      if (fis == null) {
        fis = new FileInputStream("./etc/" + yamlFile);
      }

      Yaml yaml = new Yaml(new Constructor(JdbcConfigFactory.class));
      JdbcConfigFactory factory = yaml.load(fis);

      for (JdbcConfig config : factory.getConfigs()) {
        config.resolveEnvironmentVariables();
      }

      return factory;
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
    for (JdbcConfig cfg : this.configs)
      sb.append(cfg + "\n"); 
    return sb.toString();
  }
  
  public static void main(String[] args) throws IOException {
    JdbcConfigFactory factory = createJdbcConfigFactory();
    System.out.println(factory);
    factory.serialize();
  }
}
