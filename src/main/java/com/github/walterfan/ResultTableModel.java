package com.github.walterfan;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class ResultTableModel implements TableModel {
  private String[] columnNames;
  
  private Class[] columnClasses;
  
  List<Object[]> rows = new ArrayList(30);
  
  public void addTableModelListener(TableModelListener l) {}
  
  public ResultTableModel(ResultSet rs) throws SQLException {
    ResultSetMetaData metadata = rs.getMetaData();
    int numcols = metadata.getColumnCount();
    this.columnNames = new String[numcols];
    this.columnClasses = new Class[numcols];
    for (int i = 0; i < numcols; i++) {
      this.columnNames[i] = metadata.getColumnLabel(i + 1);
      try {
        String clsName = metadata.getColumnClassName(i + 1);
        this.columnClasses[i] = Class.forName(metadata.getColumnClassName(i + 1));
      } catch (ClassNotFoundException e) {
        this.columnClasses[i] = Object.class;
      } 
      int size = metadata.getColumnDisplaySize(i + 1);
      if (size == -1)
        size = 30; 
      if (size > 500)
        size = 30; 
      int labelsize = this.columnNames[i].length();
      if (labelsize > size)
        size = labelsize; 
    } 
    while (rs.next()) {
      Object[] row = new Object[numcols];
      for (int j = 0; j < numcols; j++) {
        Object value = rs.getObject(j + 1);
        if (value != null)
          row[j] = value; 
      } 
      this.rows.add(row);
    } 
  }
  
  public Class<?> getColumnClass(int columnIndex) {
    return this.columnClasses[columnIndex];
  }
  
  public int getColumnCount() {
    return this.columnClasses.length;
  }
  
  public String getColumnName(int columnIndex) {
    return this.columnNames[columnIndex];
  }
  
  public int getRowCount() {
    return this.rows.size();
  }
  
  public Object getValueAt(int rowIndex, int columnIndex) {
    return ((Object[])this.rows.get(rowIndex))[columnIndex];
  }
  
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }
  
  public void removeTableModelListener(TableModelListener l) {}
  
  public void setValueAt(Object value, int rowIndex, int columnIndex) {}
  
  public static void main(String[] args) {}
}
