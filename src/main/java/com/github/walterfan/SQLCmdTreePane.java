package com.github.walterfan;

import java.awt.Component;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class SQLCmdTreePane extends JPanel implements TreeSelectionListener {
  private static final long serialVersionUID = 1L;
  
  private TreeSelectionListener treeListener;
  
  private Map<String, SQLCommands> sqlCmdGroup;
  
  private JTree sqlCmdtree;
  
  DefaultMutableTreeNode topNode;
  
  private class MyRenderer extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = 1L;
    
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      if (leaf) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
        Object nodeInfo = node.getUserObject();
        if (nodeInfo instanceof SQLCommand) {
          SQLCommand cmd = (SQLCommand)nodeInfo;
          setText(cmd.getName());
          setToolTipText(cmd.getSql());
        } 
      } 
      return this;
    }
  }
  
  public SQLCmdTreePane(String title) {
    this.topNode = new DefaultMutableTreeNode(title);
  }
  
  public void setTreeListener(TreeSelectionListener listener) {
    this.treeListener = listener;
  }
  
  public void setSqlCmdGroup(Map<String, SQLCommands> httpCmdGroup) {
    this.sqlCmdGroup = httpCmdGroup;
  }
  
  public JTree getHttpCmdTree() {
    return this.sqlCmdtree;
  }
  
  public void init() {
    createNodes(this.topNode);
    this.sqlCmdtree = new JTree(this.topNode);
    this.sqlCmdtree.addTreeSelectionListener(this.treeListener);
    this.sqlCmdtree.setCellRenderer(new MyRenderer());
    JScrollPane treeView = new JScrollPane(this.sqlCmdtree);
    add(treeView, "Center");
  }
  
  private void createNodes(DefaultMutableTreeNode top) {
    for (Map.Entry<String, SQLCommands> entry : this.sqlCmdGroup.entrySet()) {
      DefaultMutableTreeNode category = new DefaultMutableTreeNode(entry.getKey());
      top.add(category);
      SQLCommands group = entry.getValue();
      for (Map.Entry<String, SQLCommand> entry0 : (Iterable<Map.Entry<String, SQLCommand>>)group) {
        DefaultMutableTreeNode apiNode = new DefaultMutableTreeNode(entry0.getValue());
        category.add(apiNode);
      } 
    } 
  }
  
  public void valueChanged(TreeSelectionEvent e) {
    this.treeListener.valueChanged(e);
  }
}
