package com.github.walterfan.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

public class MyFilter extends FileFilter {
  private String[] arrSuffix;
  
  private String desc;
  
  public MyFilter(String[] arrSuffix, String desc) {
    this.arrSuffix = arrSuffix;
    this.desc = desc;
  }
  
  public boolean accept(File f) {
    if (f.isDirectory())
      return true; 
    String extension = FilenameUtils.getExtension(f.getName());
    if (extension != null)
      for (int i = 0; i < this.arrSuffix.length; i++) {
        if (StringUtils.endsWithIgnoreCase(extension, this.arrSuffix[i]))
          return true; 
      }  
    return false;
  }
  
  public String getDescription() {
    return this.desc;
  }
}
