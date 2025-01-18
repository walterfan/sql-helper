package com.github.walterfan;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.github.walterfan.db.DbConfig;
import com.github.walterfan.swing.ActionHandlerFactory;
import com.github.walterfan.swing.SwingTool;
import com.github.walterfan.swing.SwingUtils;
import com.github.walterfan.util.ClassPathHacker;
import com.github.walterfan.util.ConfigLoader;
import com.github.walterfan.util.MyFilter;
import com.github.walterfan.util.StringUtil;
import com.github.walterfan.util.TextTransfer;

public class SqlHelper extends SwingTool {
  private static final long serialVersionUID = -5757194742469820513L;
  
  private static final String NAME_VER = "SQL Tool v1.0";
  
  private static final Color DEFAULT_COLOR = new Color(153, 255, 204);
  
  private MyFilter sqlFileFilter = new MyFilter(new String[] { "sql" }, "sql");
  
  private MyFilter csvFileFilter = new MyFilter(new String[] { "csv" }, "csv");
  
  private MyFilter xmlFileFilter = new MyFilter(new String[] { "xml" }, "xml");
  
  private ConfigLoader cfgLoader = ConfigLoader.getInstance();
  
  private class MyKeyListener extends KeyAdapter {
    private MyKeyListener() {}
    
    public void keyPressed(KeyEvent evt) {
      int keyCode = evt.getKeyCode();
      if (keyCode == 116) {
        SqlHelper.this.executeSql();
      } else if (keyCode == 117) {
        SqlHelper.this.executeSqlBlock();
      } 
    }
  }
  
  private class ResetHandler implements ActionListener {
    private ResetHandler() {}
    
    public void actionPerformed(ActionEvent e) {
      SqlHelper.this.txtSQL.setText("");
      while (SqlHelper.this.sqlTab.getColumnCount() > 0)
        SqlHelper.this.sqlTab.removeColumn(SqlHelper.this.sqlTab.getColumnModel().getColumn(0)); 
      SqlHelper.this.setSql.clear();
      DefaultListModel model = (DefaultListModel)SqlHelper.this.sqlList.getModel();
      while (model.getSize() > 0)
        model.remove(0); 
      SqlHelper.this.modelFactory.close();
    }
  }
  
  private class CloseHandler implements ActionListener {
    private CloseHandler() {}
    
    public void actionPerformed(ActionEvent e) {
      SqlHelper.this.modelFactory.close();
      SqlHelper.this.msgline.setText("Closed connection.");
    }
  }
  
  private class ExitHandler implements ActionListener {
    private ExitHandler() {}
    
    public void actionPerformed(ActionEvent event) {
      if (SqlHelper.this.modelFactory != null)
        SqlHelper.this.modelFactory.close(); 
      System.exit(0);
    }
  }
  
  private class ExecuteScriptHandler implements ActionListener {
    private ExecuteScriptHandler() {}
    
    public void actionPerformed(ActionEvent e) {
      SqlHelper.this.executeSqlBlock();
    }
  }
  
  private class CommitHandler implements ActionListener {
    private CommitHandler() {}
    
    public void actionPerformed(ActionEvent e) {
      SqlHelper.this.executeAndDisplayResults("commit");
    }
  }
  
  private class RollbackHandler implements ActionListener {
    private RollbackHandler() {}
    
    public void actionPerformed(ActionEvent e) {
      SqlHelper.this.executeAndDisplayResults("rollback");
    }
  }
  
  private class LoadSqlTplHandler implements ActionListener {
    private LoadSqlTplHandler() {}
    
    public void actionPerformed(ActionEvent e) {
      JFileChooser c = new JFileChooser("./etc");
      c.setFileFilter((FileFilter)SqlHelper.this.xmlFileFilter);
      int rVal = c.showOpenDialog((Component)SqlHelper.this);
      if (rVal == 0) {
        File file = c.getSelectedFile();
        SqlHelper.this.curSqlTplName = file.getAbsolutePath();
        SqlHelper.this.sqlTemplate = null;
        SqlHelper.this.sqlTemplate = new SQLTemplate();
        try {
          SqlHelper.this.createSqlMenuAndTree(SqlHelper.this.curSqlTplName);
          SqlHelper.this.sqlMenu.repaint();
          SwingUtils.prompt("Load SQL", "loaded " + SqlHelper.this.curSqlTplName);
          int fsize = SqlHelper.this.recentSqlFiles.size();
          if (fsize == 0) {
            SqlHelper.this.fileMenu.addSeparator();
          } else if (fsize > 4) {
            SqlHelper.this.recentSqlFiles.removeFirst();
          } 
          SqlHelper.this.recentSqlFiles.addLast(SqlHelper.this.curSqlTplName);
          JMenuItem tplItem = new JMenuItem(SqlHelper.this.curSqlTplName);
          SqlHelper.this.fileMenu.add(tplItem);
        } catch (Exception e1) {
          e1.printStackTrace();
          SwingUtils.alert(e1.getMessage());
        } 
      } 
    }
  }
  
  private class LoadIbatisCfgHandler implements ActionListener {
    private LoadIbatisCfgHandler() {}
    
    public void actionPerformed(ActionEvent e) {
      String classpath = SqlHelper.this.cfgLoader.getProperty("iBatisClassPath");
      if (StringUtils.isNotBlank(classpath))
        try {
          ClassPathHacker.addFile(new File(classpath));
        } catch (IOException e2) {
          e2.printStackTrace();
        }  
      JFileChooser c = null;
      String filename = SqlHelper.this.cfgLoader.getProperty("iBatisSqlTemplate");
      if (StringUtils.isNotBlank(filename)) {
        String dir = FilenameUtils.getPath(filename);
        c = new JFileChooser(dir);
      } else {
        c = new JFileChooser("./etc");
      } 
      c.setFileFilter((FileFilter)SqlHelper.this.xmlFileFilter);
      int rVal = c.showOpenDialog((Component)SqlHelper.this);
      if (rVal == 0) {
        File file = c.getSelectedFile();
        SqlHelper.this.curSqlTplName = file.getAbsolutePath();
        SqlHelper.this.sqlTemplate = new IbatisSQLTemplate();
        try {
          SqlHelper.this.createSqlMenuAndTree(SqlHelper.this.curSqlTplName);
          SqlHelper.this.sqlMenu.repaint();
          SwingUtils.prompt("Load SQL", "loaded " + SqlHelper.this.curSqlTplName);
          int fsize = SqlHelper.this.recentIbatisFiles.size();
          if (fsize == 0) {
            SqlHelper.this.fileMenu.addSeparator();
          } else if (fsize > 4) {
            SqlHelper.this.recentIbatisFiles.removeFirst();
          } 
          SqlHelper.this.recentIbatisFiles.addLast(SqlHelper.this.curSqlTplName);
          JMenuItem tplItem = new JMenuItem(SqlHelper.this.curSqlTplName);
          SqlHelper.this.fileMenu.add(tplItem);
        } catch (Exception e1) {
          e1.printStackTrace();
          SwingUtils.alert(e1.getMessage());
        } 
      } 
    }
  }
  
  private class ReloadSqlTplHandler implements ActionListener {
    private ReloadSqlTplHandler() {}
    
    public void actionPerformed(ActionEvent e) {
      String filename = SqlHelper.this.curSqlTplName;
      try {
        SqlHelper.this.createSqlMenuAndTree(filename);
        SqlHelper.this.sqlMenu.repaint();
        SwingUtils.prompt("Reload SQL", "reloaded " + filename);
      } catch (Exception e1) {
        e1.printStackTrace();
        SwingUtils.alert(e1.getMessage());
      } 
    }
  }
  
  private class LoadHandler implements ActionListener {
    private LoadHandler() {}
    
    public void actionPerformed(ActionEvent e) {
      JFileChooser c = new JFileChooser("./log");
      c.setFileFilter((FileFilter)SqlHelper.this.sqlFileFilter);
      int rVal = c.showOpenDialog((Component)SqlHelper.this);
      if (rVal == 0) {
        File file = c.getSelectedFile();
        try {
          List<String> aList = FileUtils.readLines(file);
          DefaultListModel<String> model = (DefaultListModel)SqlHelper.this.sqlList.getModel();
          for (String aSql : aList) {
            String sql = StringUtil.trimTrailingCharacter(aSql, ';');
            boolean isNew = SqlHelper.this.setSql.add(sql);
            if (isNew)
              model.add(model.getSize(), sql); 
          } 
          SwingUtils.prompt("Load SQL", "loaded " + aList.size() + " SQL.");
        } catch (IOException e1) {
          e1.printStackTrace();
          SwingUtils.alert(e1.getMessage());
        } 
      } 
    }
  }
  
  private class AddPathHandler implements ActionListener {
    private AddPathHandler() {}
    
    public void actionPerformed(ActionEvent e) {
      JFileChooser c = new JFileChooser(SqlHelper.this.cfgLoader.getProperty("classpath", "/"));
      c.setFileSelectionMode(2);
      c.setDialogTitle("Select path into classpath");
      int rVal = c.showOpenDialog((Component)SqlHelper.this);
      if (rVal == 0) {
        File file = c.getSelectedFile();
        try {
          ClassPathHacker.addFile(file);
        } catch (IOException e1) {
          e1.printStackTrace();
          SwingUtils.alert(e1.getMessage());
        } 
      } 
    }
  }
  
  private class ExportHandler implements ActionListener {
    private ExportHandler() {}
    
    public void actionPerformed(ActionEvent e) {
      TableModel model = SqlHelper.this.sqlTab.getModel();
      if (model.getRowCount() == 0) {
        SwingUtils.alert("There is no data.");
        return;
      } 
      JFileChooser c = new JFileChooser("./log");
      c.setFileFilter((FileFilter)SqlHelper.this.csvFileFilter);
      int rVal = c.showSaveDialog((Component)SqlHelper.this);
      if (rVal == 0) {
        FileOutputStream fs = null;
        try {
          String filename = c.getSelectedFile().getAbsolutePath();
          fs = new FileOutputStream(filename);
          String resultType = (String)SqlHelper.this.resultTypeList.getSelectedItem();
          if ("CSV".equals(resultType)) {
            SqlHelper.this.saveTableModelAsCsv(fs, model);
          } else {
            SqlHelper.this.saveTableModelAsSql(fs, model);
          } 
          SwingUtils.prompt("Saved CSV", "saved " + filename);
        } catch (FileNotFoundException e1) {
          e1.printStackTrace();
          SwingUtils.alert(e1.getMessage());
        } catch (IOException e2) {
          e2.printStackTrace();
          SwingUtils.alert(e2.getMessage());
        } finally {
          IOUtils.closeQuietly(fs);
        } 
      } 
    }
  }
  
  private class CopyHandler implements ActionListener {
    private CopyHandler() {}
    
    public void actionPerformed(ActionEvent e) {
      TableModel model = SqlHelper.this.sqlTab.getModel();
      if (model.getRowCount() == 0) {
        SwingUtils.alert("There is no data.");
        return;
      } 
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      try {
        String resultType = (String)SqlHelper.this.resultTypeList.getSelectedItem();
        if ("CSV".equals(resultType)) {
          SqlHelper.this.saveTableModelAsCsv(bs, model);
        } else {
          SqlHelper.this.saveTableModelAsSql(bs, model);
        } 
      } catch (IOException e2) {
        e2.printStackTrace();
        SwingUtils.alert(e2.getMessage());
      } 
      SwingUtils.prompt("Copied to Clipboard", "Copied to Clipboard");
      TextTransfer trans = new TextTransfer();
      trans.setClipboardContents(bs.toString());
    }
  }
  
  private class SaveHandler implements ActionListener {
    private SaveHandler() {}
    
    public void actionPerformed(ActionEvent actionEvent) {
      JFileChooser c = new JFileChooser("./log");
      c.setFileFilter((FileFilter)SqlHelper.this.sqlFileFilter);
      int rVal = c.showSaveDialog((Component)SqlHelper.this);
      if (rVal == 0) {
        FileOutputStream fs = null;
        try {
          String filename = c.getSelectedFile().getAbsolutePath();
          fs = new FileOutputStream(filename);
          if (SqlHelper.this.setSql.isEmpty() && 
            StringUtils.isNotBlank(SqlHelper.this.txtSQL.getText())) {
            fs.write(SqlHelper.this.txtSQL.getText().getBytes());
            fs.write(";\n".getBytes());
          } else {
            for (String sql : SqlHelper.this.setSql) {
              fs.write(sql.getBytes());
              fs.write(";\n".getBytes());
            } 
          } 
          SwingUtils.prompt("Saved SQL", "saved " + filename);
        } catch (FileNotFoundException e1) {
          e1.printStackTrace();
          SwingUtils.alert(e1.getMessage());
        } catch (IOException e2) {
          e2.printStackTrace();
          SwingUtils.alert(e2.getMessage());
        } finally {
          IOUtils.closeQuietly(fs);
        } 
      } 
    }
  }
  
  private class SqlMenuHandler implements ActionListener {
    private String sqlType;
    
    private String sqlName;
    
    public SqlMenuHandler(String sqlType, String sqlName) {
      this.sqlType = sqlType;
      this.sqlName = sqlName;
    }
    
    public void actionPerformed(ActionEvent event) {
      SQLCommand cmd = SqlHelper.this.sqlTemplate.getSQLCommand(this.sqlType, this.sqlName);
      SqlHelper.this.txtSQL.setText(cmd.getSql());
    }
  }
  
  private class SubmitHandler implements ActionListener {
    private SubmitHandler() {}

    public void actionPerformed(ActionEvent e) {
      SqlHelper.this.executeSql();
    }
  }
  
  private class DbListHandler implements ActionListener {
    private DbListHandler() {}
    
    public void actionPerformed(ActionEvent e) {
      String name = (String)SqlHelper.this.dbList.getSelectedItem();
      JdbcConfig cfg = SqlHelper.this.jdbcFactory.get(name);
      if (cfg == null)
        return; 
      SqlHelper.this.txtDriver.setText(cfg.getDriverClass());
      SqlHelper.this.txtDbType.setText(cfg.getType());
      SqlHelper.this.txtUrl.setText(cfg.getUrl());
      SqlHelper.this.txtUsername.setText(cfg.getUserName());
      SqlHelper.this.txtPassword.setText(cfg.getPassword());
    }
  }
  
  private class SQLListSelectionListener implements ListSelectionListener {
    private SQLListSelectionListener() {}
    
    public void valueChanged(ListSelectionEvent evt) {
      if (!evt.getValueIsAdjusting()) {
        JList list = (JList)evt.getSource();
        Object[] selected = list.getSelectedValues();
        if (selected != null && selected.length > 0)
          SqlHelper.this.txtSQL.setText((String)selected[0]); 
      } 
    }
  }
  
  private class SelectTreeHandler implements TreeSelectionListener {
    private SQLCmdTreePane cmdTreePane;
    
    public SelectTreeHandler(SQLCmdTreePane pane) {
      this.cmdTreePane = pane;
    }
    
    public void valueChanged(TreeSelectionEvent e) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)this.cmdTreePane.getHttpCmdTree().getLastSelectedPathComponent();
      if (node != null) {
        Object nodeInfo = node.getUserObject();
        if (node.isLeaf()) {
          SQLCommand cmd = (SQLCommand)nodeInfo;
          SqlHelper.this.txtSQL.setText(cmd.getSql());
        } 
      } 
    }
  }
  
  private JdbcConfigFactory jdbcFactory = null;
  
  private DbConfig currentDbCfg = null;
  
  private SQLTemplate sqlTemplate = new SQLTemplate();
  
  private Set<String> setSql = new HashSet<>(10);
  
  private int defaultIdx = 0;
  
  private String defaultSql = "select sysdate from dual";
  
  private String curSqlTplName = "SQLTemplate.xml";
  
  private LinkedList<String> recentSqlFiles = new LinkedList<>();
  
  private LinkedList<String> recentIbatisFiles = new LinkedList<>();
  
  private JMenu fileMenu = new JMenu("Usage");
  
  private JMenu sqlMenu = new JMenu("SQL");
  
  private JList sqlList = null;
  
  private JMenuBar menuBar = new JMenuBar();
  
  private JPanel sqlPanel = new JPanel(new BorderLayout());
  
  private JScrollPane sqlTreePane;
  
  ResultSetTableModelFactory modelFactory;
  
  JComboBox dbList = null;
  
  JTextField txtDriver = new JTextField();
  
  JTextField txtDbType = new JTextField();
  
  JTextField txtUsername = new JTextField();
  
  JPasswordField txtPassword = new JPasswordField();
  
  JTextArea txtUrl = new JTextArea();
  
  JButton btnSubmit = new JButton("execute");
  
  JButton btnReset = new JButton("clear");
  
  JButton btnSave = new JButton("save");
  
  JButton btnCommit = new JButton("commit");
  
  JButton btnRollback = new JButton("rollback");
  
  JButton btnLoad = new JButton("load");
  
  JButton btnClose = new JButton("close");
  
  JButton btnCopy = new JButton("copy");
  
  JButton btnExport = new JButton("export");
  
  JButton btnExit = new JButton("exit");
  
  private JComboBox resultTypeList;
  
  JTextArea txtSQL = new JTextArea(4, 240);
  
  JTable sqlTab = new JTable() {
      public String getToolTipText(MouseEvent e) {
        String tip = null;
        Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
        int realColumnIndex = convertColumnIndexToModel(colIndex);
        tip = "" + getModel().getValueAt(rowIndex, realColumnIndex);
        tip = "<html>" + StringUtil.nl2br(StringEscapeUtils.escapeHtml(tip)) + "</html>";
        return tip;
      }
    };
  
  JTextArea msgline = new JTextArea();
  
  public SqlHelper() {
    this("SQL Tool v1.0");
  }
  
  public SqlHelper(String title) {
    this(title, new ResultSetTableModelFactory());
  }
  
  public SqlHelper(String title, ResultSetTableModelFactory factory) {
    super(title);
    this.modelFactory = factory;
    setDefaultCloseOperation(3);
    setFocusable(true);
    addKeyListener(new MyKeyListener());
    loadConfig();
    createDbList();
    createResultTypeList();
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout(10, 10));
    JPanel cfgPanel = createCfgPanel();
    JPanel sqlPanel = createSqlPanel();
    JSplitPane splitPane = createtSplitPanel(cfgPanel, sqlPanel);
    splitPane.setPreferredSize(new Dimension(getWidth(), 140));
    JScrollPane sqlTabPane = new JScrollPane(this.sqlTab, 20, 30);
    sqlTabPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    this.sqlTab.setBackground(DEFAULT_COLOR);
    this.sqlTab.setAutoResizeMode(0);
    JSplitPane topPane = new JSplitPane(0, splitPane, sqlTabPane);
    topPane.setOneTouchExpandable(true);
    topPane.setDividerLocation(300);
    contentPane.add(topPane, "Center");
    JPopupMenu rightMenu = SwingUtils.createStdEditPopupMenu(new JTextComponent[] { this.txtSQL });
    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new BorderLayout());
    this.msgline.setRows(2);
    JScrollPane msgPane = new JScrollPane(this.msgline);
    bottomPanel.add(msgPane, "Center");
    this.btnCopy.addActionListener(new CopyHandler());
    this.btnExport.addActionListener(new ExportHandler());
    this.btnExit.addActionListener(new ExitHandler());
    JPanel bottomLeftPanel = SwingUtils.createHorizontalPanel(new Component[] { this.btnCopy, this.btnExport, this.resultTypeList });
    bottomPanel.add(bottomLeftPanel, "West");
    bottomPanel.add(this.btnExit, "East");
    contentPane.add(bottomPanel, "South");
    init();
  }
  
  private void createResultTypeList() {
    Vector<String> vec = new Vector<>();
    vec.add("CSV");
    vec.add("SQL");
    this.resultTypeList = new JComboBox<>(vec);
  }
  
  private void loadConfig() {
    try {
      this.cfgLoader.loadFromClassPath("db_querier.properties", getClass().getClassLoader());
      this.defaultSql = this.cfgLoader.getProperty("defaultSql", this.defaultSql);
      this.defaultIdx = NumberUtils.toInt(this.cfgLoader.getProperty("defaultIndex"));
      this.curSqlTplName = this.cfgLoader.getProperty("defaultSqlTemplate", this.curSqlTplName);
      this.jdbcFactory = JdbcConfigFactory.deserialize();
    } catch (Exception e) {
      e.printStackTrace();
      this.jdbcFactory = JdbcConfigFactory.createJdbcConfigFactory();
    }
  }
  
  private JPanel createSqlPanel() {
    this.sqlPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    this.txtSQL.setText(this.defaultSql);
    this.txtSQL.setLineWrap(true);
    this.txtSQL.setBackground(DEFAULT_COLOR);
    this.sqlList = new JList(new DefaultListModel());
    this.sqlList.setVisibleRowCount(5);
    this.sqlList.ensureIndexIsVisible(2);
    this.sqlList.setSelectedIndex(0);
    this.sqlList.setFixedCellWidth(60);
    this.sqlList.addListSelectionListener(new SQLListSelectionListener());
    this.sqlList.setBackground(DEFAULT_COLOR);
    return this.sqlPanel;
  }
  
  private void createDbList() {
    List<JdbcConfig> list = this.jdbcFactory.getConfigList();
    Vector<String> vec = new Vector<>(list.size());
    for (int i = 0; i < list.size(); i++) {
      JdbcConfig cfg = list.get(i);
      if (cfg != null) {
        if (i == this.defaultIdx) {
          this.txtDriver.setText(cfg.getDriverClass());
          this.txtDbType.setText(cfg.getType());
          this.txtUrl.setText(cfg.getUrl());
          this.txtUsername.setText(cfg.getUserName());
          this.txtPassword.setText(cfg.getPassword());
        } 
        vec.add(cfg.getName());
      } 
    } 
    JComboBox<String> cfgList = new JComboBox<>(vec);
    cfgList.setEditable(true);
    if (vec.size() > 0)
      cfgList.setSelectedIndex(this.defaultIdx); 
    cfgList.addActionListener(new DbListHandler());
    this.dbList = cfgList;
  }
  
  public void initComponents() {
    JSplitPane leftPane = new JSplitPane(1, true, new JScrollPane(this.txtSQL), new JScrollPane(this.sqlList));
    leftPane.setOneTouchExpandable(true);
    leftPane.setDividerLocation(400);
    JSplitPane topPane = new JSplitPane(1, true, leftPane, this.sqlTreePane);
    topPane.setOneTouchExpandable(true);
    topPane.setDividerLocation(500);
    this.sqlPanel.add(topPane, "Center");
    createButtonPanel();
  }
  
  public void initTopMenu(JMenuBar aMenuBar) {
    setJMenuBar(this.menuBar);
    aMenuBar.add(this.fileMenu);
    JMenuItem cfgItem = new JMenuItem("Load SQL Template");
    cfgItem.addActionListener(new LoadSqlTplHandler());
    this.fileMenu.add(cfgItem);
    JMenuItem ibatisItem = new JMenuItem("Load ibatis sqlmap Config");
    ibatisItem.addActionListener(new LoadIbatisCfgHandler());
    this.fileMenu.add(ibatisItem);
    JMenuItem pathItem = new JMenuItem("Add Class Path");
    pathItem.addActionListener(new AddPathHandler());
    this.fileMenu.add(pathItem);
    JMenuItem reloadItem = new JMenuItem("Reload current Template");
    reloadItem.addActionListener(new ReloadSqlTplHandler());
    this.fileMenu.add(reloadItem);
    this.fileMenu.addSeparator();
    JMenuItem executeItem = new JMenuItem("Execute SQL");
    executeItem.setAccelerator(KeyStroke.getKeyStroke(116, 0, false));
    executeItem.addActionListener(new SubmitHandler());
    this.fileMenu.add(executeItem);
    JMenuItem scriptItem = new JMenuItem("Execute SQL as script");
    scriptItem.addActionListener(new ExecuteScriptHandler());
    scriptItem.setAccelerator(KeyStroke.getKeyStroke(117, 0, false));
    this.fileMenu.add(scriptItem);
    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(new ExitHandler());
    exitItem.setAccelerator(KeyStroke.getKeyStroke(88, 8, false));
    this.fileMenu.add(exitItem);
    createSqlMenuAndTree(this.curSqlTplName);
    aMenuBar.add(this.sqlMenu);
    JMenu helpMenu = new JMenu("Help");
    aMenuBar.add(helpMenu);
    JMenuItem helpItem = new JMenuItem("Help");
    helpItem.addActionListener(ActionHandlerFactory.createAboutHandler((JFrame)this, "Help of SQL Tool v1.0", " Just like PHP database tool ", 240, 100));
    JMenuItem aboutItem = new JMenuItem("About");
    aboutItem.addActionListener(ActionHandlerFactory.createAboutHandler((JFrame)this, "About SQL Tool v1.0", " Wrote by Walter Fan, updated on 07/11/09 ", 320, 100));
    helpMenu.add(helpItem);
    helpMenu.add(aboutItem);
  }
  
  private void createSqlMenuAndTree(String xmlTpl) {
    this.sqlMenu.removeAll();
    try {
      this.sqlTemplate.clear();
      this.sqlTemplate.loadFromResource(xmlTpl);
      Map<String, SQLCommands> sqlMap = this.sqlTemplate.getSqlMap();
      for (Map.Entry<String, SQLCommands> entry : sqlMap.entrySet()) {
        String sqlType = entry.getKey();
        JMenu sqlSubMenu = new JMenu(sqlType);
        this.sqlMenu.add(sqlSubMenu);
        Collection<SQLCommand> sqls = ((SQLCommands)entry.getValue()).getSqlMap().values();
        Object[] arrSqls = sqls.toArray();
        Arrays.sort(arrSqls);
        for (Object cmd : arrSqls) {
          SQLCommand sqlCmd = (SQLCommand)cmd;
          JMenuItem cmdMenuItem = new JMenuItem(sqlCmd.getName());
          sqlSubMenu.add(cmdMenuItem);
          cmdMenuItem.addActionListener(new SqlMenuHandler(sqlType, sqlCmd
                .getName()));
        } 
      } 
      SQLCmdTreePane treePane = new SQLCmdTreePane("Frequently-used SQLs");
      treePane.setSqlCmdGroup(sqlMap);
      treePane.setTreeListener(new SelectTreeHandler(treePane));
      treePane.init();
      this.sqlTreePane = new JScrollPane(treePane);
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  private void saveTableModelAsCsv(OutputStream os, TableModel model) throws IOException {
    int i;
    for (i = 0; i < model.getColumnCount(); i++) {
      if (i > 0)
        os.write(",".getBytes()); 
      os.write(model.getColumnName(i).getBytes());
    } 
    os.write("\n".getBytes());
    for (i = 0; i < model.getRowCount(); i++) {
      for (int j = 0; j < model.getColumnCount(); j++) {
        Object value = model.getValueAt(i, j);
        if (value == null)
          value = "null"; 
        if (j > 0)
          os.write(",".getBytes()); 
        os.write(value.toString().getBytes());
      } 
      os.write("\n".getBytes());
    } 
  }
  
  private void saveTableModelAsSql(OutputStream os, TableModel model) throws IOException {
    os.write("insert into tableName (".getBytes());
    int i;
    for (i = 0; i < model.getColumnCount(); i++) {
      if (i > 0)
        os.write(",".getBytes()); 
      os.write(model.getColumnName(i).getBytes());
    } 
    os.write(")\n values (".getBytes());
    for (i = 0; i < model.getRowCount(); i++) {
      for (int j = 0; j < model.getColumnCount(); j++) {
        Object value = model.getValueAt(i, j);
        if (value == null)
          value = "null"; 
        if (j > 0)
          os.write(",".getBytes()); 
        os.write(value.toString().getBytes());
      } 
      os.write(")\n".getBytes());
    } 
  }
  
  private JPanel createButtonPanel() {
    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new GridLayout(1, 6));
    this.btnSave.addActionListener(new SaveHandler());
    insertBtn2ToolbarAndMenu("save", this.btnSave);
    this.btnLoad.addActionListener(new LoadHandler());
    insertBtn2ToolbarAndMenu("load", this.btnLoad);
    insertBtn2ToolbarAndMenu("close", this.btnClose);
    this.btnClose.setToolTipText("close connection");
    this.btnClose.addActionListener(new CloseHandler());
    insertBtn2ToolbarAndMenu("rollback", this.btnRollback);
    this.btnRollback.setToolTipText("Rollback the SQL operation");
    this.btnRollback.addActionListener(new RollbackHandler());
    insertBtn2ToolbarAndMenu("commit", this.btnCommit);
    this.btnCommit.setToolTipText("commit the SQL operation");
    this.btnCommit.addActionListener(new CommitHandler());
    insertBtn2ToolbarAndMenu("clear", this.btnReset);
    this.btnReset.setToolTipText("Clear SQL and the history");
    this.btnReset.addActionListener(new ResetHandler());
    insertBtn2ToolbarAndMenu("execute", this.btnSubmit);
    this.btnSubmit.setToolTipText("Execute SQL");
    this.btnSubmit.addActionListener(new SubmitHandler());
    return btnPanel;
  }
  
  private JPanel createCfgPanel() {
    JPanel cfgPanel = new JPanel();
    cfgPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    Box cfgBox = Box.createVerticalBox();
    Dimension labelDim = new Dimension(80, 20);
    JPanel panel0 = new JPanel(new BorderLayout());
    JLabel label0 = new JLabel("Database: ", 4);
    label0.setPreferredSize(labelDim);
    panel0.add(label0, "West");
    panel0.add(this.dbList, "Center");
    Dimension dim = new Dimension(30, 20);
    this.txtDbType.setMinimumSize(dim);
    this.txtDbType.setHorizontalAlignment(2);
    panel0.add(this.txtDbType, "East");
    cfgBox.add(Box.createVerticalGlue());
    cfgBox.add(panel0);
    JPanel panel1 = new JPanel(new BorderLayout());
    JLabel label1 = new JLabel("Driver: ", 4);
    label1.setPreferredSize(labelDim);
    panel1.add(label1, "West");
    panel1.add(this.txtDriver, "Center");
    cfgBox.add(Box.createVerticalGlue());
    cfgBox.add(panel1);
    JPanel panel2 = new JPanel(new BorderLayout());
    JLabel label2 = new JLabel("URL: ", 4);
    label2.setPreferredSize(labelDim);
    panel2.add(label2, "West");
    cfgBox.add(Box.createVerticalGlue());
    this.txtUrl.setRows(2);
    this.txtUrl.setLineWrap(true);
    panel2.add(new JScrollPane(this.txtUrl), "Center");
    cfgBox.add(panel2);
    cfgBox.add(Box.createVerticalGlue());
    JPanel panel3 = new JPanel(new BorderLayout());
    JLabel label3 = new JLabel("Username: ", 4);
    label3.setPreferredSize(labelDim);
    panel3.add(label3, "West");
    panel3.add(this.txtUsername, "Center");
    cfgBox.add(panel3);
    cfgBox.add(Box.createVerticalGlue());
    JPanel panel4 = new JPanel(new BorderLayout());
    JLabel label4 = new JLabel("Password: ", 4);
    label4.setPreferredSize(labelDim);
    panel4.add(label4, "West");
    panel4.add(this.txtPassword, "Center");
    cfgBox.add(panel4);
    cfgPanel.setLayout(new BorderLayout());
    cfgPanel.add(cfgBox, "Center");
    return cfgPanel;
  }
  
  private JSplitPane createtSplitPanel(JPanel cfgPanel, JPanel sqlPanel) {
    JSplitPane splitPane = new JSplitPane(1, cfgPanel, sqlPanel);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(0.5D);
    Dimension minimumSize = new Dimension(100, 50);
    sqlPanel.setMinimumSize(minimumSize);
    splitPane.setPreferredSize(new Dimension(400, 100));
    return splitPane;
  }
  
  public void executeAndDisplayResults(final String sql) {
    if (StringUtils.isBlank(sql)) {
      SwingUtils.alert("Please input an SQL at least.");
      return;
    } 
    this.msgline.setText("Contacting database...");
    EventQueue.invokeLater(new Runnable() {
          public void run() {
            try {
              long beginTime = System.currentTimeMillis();
              TableModel model = SqlHelper.this.modelFactory.execute4TableModel(sql);
              SqlHelper.this.sqlTab.setModel(model);
              SqlHelper.this.sqlTab.setColumnSelectionAllowed(true);
              SqlHelper.this.sqlTab.setRowSelectionAllowed(true);
              int colCnt = model.getColumnCount();
              StringBuilder colSb = new StringBuilder();
              for (int i = 0; i < colCnt; i++) {
                colSb.append(model.getColumnName(i));
                colSb.append(", ");
              } 
              SqlHelper.this.msgline.setText("Row count: " + model.getRowCount() + ", column count: " + colCnt + ", columns: " + colSb
                  
                  .toString() + "Duration(ms): " + (
                  
                  System.currentTimeMillis() - beginTime));
            } catch (Exception ex) {
              ex.printStackTrace();
              SqlHelper.this.msgline.setText(ex.getMessage());
              JOptionPane.showMessageDialog((Component)SqlHelper.this, new String[] { ex
                    
                    .getClass().getName() + ": ", ex.getMessage() });
            } 
          }
        });
  }
  
  private void executeSqlBlock() {
    try {
      checkDbConfig();
    } catch (ClassNotFoundException e) {
      SwingUtils.alert(e.getMessage());
      return;
    } 
    EventQueue.invokeLater(new Runnable() {
          public void run() {
            try {
              String sql = SqlHelper.this.txtSQL.getText();
              if (StringUtils.isBlank(sql)) {
                SwingUtils.alert("Please input an SQL at least.");
                return;
              } 
              long beginTime = System.currentTimeMillis();
              TableModel model = SqlHelper.this.modelFactory.executeSqlBlock(sql);
              SqlHelper.this.sqlTab.setModel(model);
              SqlHelper.this.sqlTab.setColumnSelectionAllowed(true);
              SqlHelper.this.sqlTab.setRowSelectionAllowed(true);
              SqlHelper.this.msgline.setText("Row count: " + model.getRowCount() + ", duration(ms): " + (
                  
                  System.currentTimeMillis() - beginTime));
            } catch (Exception ex) {
              ex.printStackTrace();
              SqlHelper.this.msgline.setText(ex.getMessage());
              JOptionPane.showMessageDialog((Component)SqlHelper.this, new String[] { ex
                    .getClass().getName() + ": ", ex.getMessage() });
            } 
          }
        });
  }
  
  private void executeSql() {
    String sql = this.txtSQL.getSelectedText();
    if (StringUtils.isBlank(sql))
      sql = this.txtSQL.getText(); 
    try {
      checkDbConfig();
    } catch (ClassNotFoundException e) {
      SwingUtils.alert(e.getMessage());
      return;
    } 
    this.msgline.setText("Execute " + sql);
    executeAndDisplayResults(sql);
    pushSql(sql);
  }
  
  private void checkDbConfig() throws ClassNotFoundException {
    DbConfig thisCfg = new DbConfig(this.txtDriver.getText(), this.txtUrl.getText(), this.txtUsername.getText(), this.txtPassword.getText());
    if (this.currentDbCfg == null) {
      this.currentDbCfg = thisCfg;
      this.modelFactory.setDbCfg(this.currentDbCfg);
    } else if (!this.currentDbCfg.equals(thisCfg)) {
      this.currentDbCfg = thisCfg;
      this.modelFactory.setDbCfg(this.currentDbCfg);
      this.modelFactory.close();
    } 
  }
  
  private void pushSql(String sql) {
    DefaultListModel<String> model = (DefaultListModel)this.sqlList.getModel();
    boolean isNew = this.setSql.add(sql);
    if (isNew)
      model.add(model.getSize(), sql); 
  }
  
  public static void main(String[] args) throws Exception {
    ResultSetTableModelFactory factory = null;
    if (args.length == 0) {
      factory = new ResultSetTableModelFactory();
    } else {
      factory = new ResultSetTableModelFactory(args[0], args[1], args[2], args[3]);
    } 
    SwingUtils.setFont(new Font("Dialog", 0, 13));
    SqlHelper qf = new SqlHelper("SQL Tool v1.0", factory);
    SwingUtils.run((JFrame)qf, 900, 700);
  }
}
