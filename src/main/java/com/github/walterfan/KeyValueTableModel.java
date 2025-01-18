package com.github.walterfan;


import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class KeyValueTableModel implements TableModel {
  private Object value = Integer.valueOf(0);
  
  private String key;
  
  public KeyValueTableModel(String name, Object value) {
    this.value = value;
    this.key = name;
  }
  
  public void addTableModelListener(TableModelListener tablemodellistener) {}
  
  public Class<?> getColumnClass(int i) {
    return String.class;
  }
  
  public int getColumnCount() {
    return 1;
  }
  
  public String getColumnName(int i) {
    return this.key;
  }
  
  public int getRowCount() {
    return 1;
  }
  
  public Object getValueAt(int i, int j) {
    return this.value;
  }
  
  public boolean isCellEditable(int i, int j) {
    return false;
  }
  
  public void removeTableModelListener(TableModelListener tablemodellistener) {}
  
  public void setValueAt(Object obj, int i, int j) {}
}
