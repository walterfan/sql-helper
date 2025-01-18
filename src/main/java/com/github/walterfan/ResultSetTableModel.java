package com.github.walterfan;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class ResultSetTableModel implements TableModel {
  ResultSet results;
  
  ResultSetMetaData metadata;
  
  int numcols;
  
  int numrows;
  
  ResultSetTableModel(ResultSet results) throws SQLException {
    this.results = results;
    this.metadata = results.getMetaData();
    this.numcols = this.metadata.getColumnCount();
    results.last();
    this.numrows = results.getRow();
  }
  
  public void close() {
    try {
      this.results.getStatement().close();
    } catch (SQLException sQLException) {}
  }
  
  protected void finalize() {
    close();
  }
  
  public int getColumnCount() {
    return this.numcols;
  }
  
  public int getRowCount() {
    return this.numrows;
  }
  
  public String getColumnName(int column) {
    try {
      return this.metadata.getColumnLabel(column + 1);
    } catch (SQLException e) {
      return e.toString();
    } 
  }
  
  public Class getColumnClass(int column) {
    return String.class;
  }
  
  public Object getValueAt(int row, int column) {
    try {
      this.results.absolute(row + 1);
      Object o = this.results.getObject(column + 1);
      if (o == null)
        return null; 
      return o.toString();
    } catch (SQLException e) {
      return e.toString();
    } 
  }
  
  public boolean isCellEditable(int row, int column) {
    return false;
  }
  
  public void setValueAt(Object value, int row, int column) {}
  
  public void addTableModelListener(TableModelListener l) {}
  
  public void removeTableModelListener(TableModelListener l) {}
}
