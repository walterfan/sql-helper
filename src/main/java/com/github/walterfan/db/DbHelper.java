package com.github.walterfan.db;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class DbHelper {
  public static <T> String list2String(Collection<T> list, String separator) {
    if (list == null || list.isEmpty())
      return ""; 
    StringBuffer sb = new StringBuffer("");
    Iterator<T> iter = list.iterator();
    sb.append(iter.next());
    while (iter.hasNext()) {
      T t = iter.next();
      sb.append(separator);
      sb.append(t);
    } 
    return sb.toString();
  }
  
  public static String list2String(Collection<Long> list) {
    return list2String(list, ",");
  }
  
  public static void printResultsTable(ResultSet rs, OutputStream output) throws SQLException {
    PrintWriter out = new PrintWriter(output);
    ResultSetMetaData metadata = rs.getMetaData();
    int numcols = metadata.getColumnCount();
    String[] labels = new String[numcols];
    int[] colwidths = new int[numcols];
    int[] colpos = new int[numcols];
    int linewidth = 1;
    for (int i = 0; i < numcols; i++) {
      colpos[i] = linewidth;
      labels[i] = metadata.getColumnLabel(i + 1);
      int size = metadata.getColumnDisplaySize(i + 1);
      if (size == -1)
        size = 30; 
      if (size > 500)
        size = 30; 
      int labelsize = labels[i].length();
      if (labelsize > size)
        size = labelsize; 
      colwidths[i] = size + 1;
      linewidth += colwidths[i] + 2;
    } 
    StringBuffer divider = new StringBuffer(linewidth);
    StringBuffer blankline = new StringBuffer(linewidth);
    int j;
    for (j = 0; j < linewidth; j++) {
      divider.insert(j, '-');
      blankline.insert(j, " ");
    } 
    for (j = 0; j < numcols; j++)
      divider.setCharAt(colpos[j] - 1, '+'); 
    divider.setCharAt(linewidth - 1, '+');
    out.println(divider);
    StringBuffer line = new StringBuffer(blankline.toString());
    line.setCharAt(0, '|');
    int k;
    for (k = 0; k < numcols; k++) {
      int pos = colpos[k] + 1 + (colwidths[k] - labels[k].length()) / 2;
      overwrite(line, pos, labels[k]);
      overwrite(line, colpos[k] + colwidths[k], " |");
    } 
    out.println(line);
    out.println(divider);
    while (rs.next()) {
      line = new StringBuffer(blankline.toString());
      line.setCharAt(0, '|');
      for (k = 0; k < numcols; k++) {
        Object value = rs.getObject(k + 1);
        if (value != null)
          overwrite(line, colpos[k] + 1, value.toString().trim()); 
        overwrite(line, colpos[k] + colwidths[k], " |");
      } 
      out.println(line);
    } 
    out.println(divider);
    out.flush();
  }
  
  private static void overwrite(StringBuffer b, int pos, String s) {
    int slen = s.length();
    int blen = b.length();
    if (pos + slen > blen)
      slen = blen - pos; 
    for (int i = 0; i < slen; i++)
      b.setCharAt(pos + i, s.charAt(i)); 
  }
  
  public static void main(String[] args) {
    List<Long> list = new ArrayList<>();
    for (long i = 0L; i < 10L; i++)
      list.add(Long.valueOf(i)); 
    System.out.println(list2String(list));
  }
}
