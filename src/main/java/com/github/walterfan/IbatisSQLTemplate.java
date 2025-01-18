package com.github.walterfan;


import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.mapping.StatementType;

public class IbatisSQLTemplate extends SQLTemplate {
  private static Log logger = LogFactory.getLog(IbatisSQLTemplate.class);
  
  public void loadFromStream(InputStream is) throws Exception {}
  
  public String transStatementType(StatementType st) {
    return "";
  }
  
  public static void main(String[] args) {
    String xml = "./src/main/webapp/WEB-INF/sqlmap-config.xml";
    try {
      SQLTemplate template = new IbatisSQLTemplate();
      template.loadFromXml(xml);
      System.out.println("template=" + template);
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
}
