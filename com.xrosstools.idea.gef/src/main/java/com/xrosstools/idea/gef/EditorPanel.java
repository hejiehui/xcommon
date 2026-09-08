package com.xrosstools.idea.gef;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.Tree;
import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.actions.CommandExecutor;
import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.CommandStack;
import com.xrosstools.idea.gef.control.EditorInteraction;
import com.xrosstools.idea.gef.control.KeyEventData;
import com.xrosstools.idea.gef.control.MouseEventData;
import com.xrosstools.idea.gef.extensions.ExtensionManager;
import com.xrosstools.idea.gef.extensions.ToolbarExtension;
import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.figures.Figure;
import com.xrosstools.idea.gef.parts.*;
import com.xrosstools.idea.gef.tools.ExportPngAction;
import com.xrosstools.idea.gef.tools.RedoAction;
import com.xrosstools.idea.gef.tools.SearchModelAction;
import com.xrosstools.idea.gef.tools.UndoAction;
import com.xrosstools.idea.gef.util.IPropertySource;
import com.xrosstools.idea.gef.util.PropertyTableModel;
import com.xrosstools.idea.gef.util.SimpleTableCellEditor;
import com.xrosstools.idea.gef.util.SimpleTableRenderer;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class EditorPanel<T extends IPropertySource> extends JPanel implements EditorFacade<T> {
    private JBSplitter mainPane;
    private JBSplitter diagramPane;
    private Tree treeNavigator;
    private JBTable tableProperties;

    private JScrollPane innerDiagramPane;
    private UnitPanel unitPanel;
    private AbstractGraphicalEditPart root;
    private AbstractTreeEditPart treeRoot;

    private ToolbarExtension extension;

    private DefaultTreeModel treeModel;
    private PropertyTableModel tableModel;
    private Figure lastSelected;

    private Project project;
    private PanelContentProvider<T> contentProvider;

    private AtomicBoolean saving = new AtomicBoolean(false);

    private EditorInteraction<T> editorInteraction;

    public EditorPanel(Project project, PanelContentProvider<T> contentProvider) throws Exception {
        this.project = project;
        this.contentProvider = contentProvider;
        contentProvider.setEditorPanel(this);

        editorInteraction = new EditorInteraction<T>(this, contentProvider);
        editorInteraction.setModel(loadDiagram());

        extension = ExtensionManager.createToolbarExtension(this);

        createVisual();
        registerListener();
        build();
    }

    private void createVisual() {
        setLayout(new BorderLayout());
        mainPane = new JBSplitter(true, 0.8f);
        mainPane.setDividerWidth(3);
        add(mainPane, BorderLayout.CENTER);

        mainPane.setFirstComponent(createMain());
        mainPane.setSecondComponent(createProperty());
    }

    private JComponent createMain() {
        diagramPane = new JBSplitter(false, 0.8f);
        diagramPane.setDividerWidth(3);

        diagramPane.setFirstComponent(createEditArea());
        diagramPane.setSecondComponent(createTree());

        return diagramPane;
    }

    private JComponent createEditArea() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(createPalette(), BorderLayout.WEST);
        mainPanel.add(createToolbar(), BorderLayout.NORTH);

        unitPanel = new UnitPanel();
        innerDiagramPane = new JBScrollPane(unitPanel);
        innerDiagramPane.setLayout(new ScrollPaneLayout());
        innerDiagramPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        innerDiagramPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        innerDiagramPane.getVerticalScrollBar().setUnitIncrement(50);

        mainPanel.add(innerDiagramPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private JComponent createPalette() {
        JPanel palette = new JPanel();
        GridLayout layout = new GridLayout(0, 1, 10, 0);
        palette.setLayout(layout);

        palette.add(createResetButton());
        contentProvider.buildPalette(palette);

        //Set executor to action
        int componentCount = palette.getComponentCount();
        for (int i = 0; i < componentCount; i++) {
            Component component = palette.getComponent(i);
            if (component instanceof JButton && ((JButton) component).getActionListeners()[0] instanceof Action) {
                ((Action) ((JButton) component).getActionListeners()[0]).setExecutor(editorInteraction);
            }
        }

        return palette;
    }

    private JComponent createToolbar() {
        ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup actionGroup = (DefaultActionGroup) contentProvider.createToolbar();
        createDefaultTools(actionGroup);

        actionGroup.addSeparator();

        //Add customized tools
        DefaultActionGroup extGroup = new DefaultActionGroup();
        extension.extendToolbar(extGroup);
        AnAction[] actions = extGroup.getChildActionsOrStubs();

        for (AnAction action : actions) {
            String actionId = action.getTemplatePresentation().getText();
            if (actionId != null &&
                    !SearchModelAction.NAME.equals(actionId) &&
                    !ExportPngAction.NAME.equals(actionId))
                actionGroup.add(action);
        }

        ActionToolbar toolbar = actionManager.createActionToolbar("XrossToolsToolbar", actionGroup, true);
        toolbar.setTargetComponent(this);

        return toolbar.getComponent();
    }

    private void createDefaultTools(ActionGroup group) {
        DefaultActionGroup actionGroup = (DefaultActionGroup) group;
        if (actionGroup.getChildrenCount() > 0) {
            actionGroup.addSeparator();
        }

        actionGroup.add(new UndoAction(editorInteraction));
        actionGroup.add(new RedoAction(editorInteraction));

        actionGroup.add(new SearchModelAction(editorInteraction));
        actionGroup.add(new ExportPngAction(this));

    }

    private JComponent createTree() {
        treeNavigator = new Tree();
        treeNavigator.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeNavigator.setExpandsSelectedPaths(true);

        JScrollPane treePane = new JBScrollPane(treeNavigator);
        treePane.setLayout(new ScrollPaneLayout());
        treePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        treePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        treePane.getVerticalScrollBar().setUnitIncrement(50);

        return treePane;
    }

    private JComponent createProperty() {
        tableProperties = new JBTable();
        tableModel = createTableModel(getModel());
        tableProperties.setModel(tableModel);

        JScrollPane scrollPane = new JBScrollPane(tableProperties);
        scrollPane.setLayout(new ScrollPaneLayout());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(50);

        return scrollPane;
    }

    private JButton createResetButton() {
        JButton btn = new JButton("Select", AllIcons.Actions.Back);
//        btn.setPreferredSize(new Dimension(100, 50));
        btn.setContentAreaFilled(false);
        btn.addActionListener(e -> reset());
        return btn;
    }

    private void reset() {
        editorInteraction.reset();
    }

    public void createConnection(Object connModel) {
        editorInteraction.createConnection(connModel);
    }

    public void createModel(Object model) {
        editorInteraction.createModel(model);
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    private void registerListener() {
        unitPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                editorInteraction.mousePressed(toMouseEventData(e));
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                editorInteraction.mouseReleased(toMouseEventData(e));
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                editorInteraction.mouseClicked(toMouseEventData(e));
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                editorInteraction.mouseEntered(toMouseEventData(e));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                editorInteraction.mouseExited(toMouseEventData(e));
            }
        });

        unitPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                editorInteraction.mouseMoved(toMouseEventData(e));
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                editorInteraction.mouseDragged(toMouseEventData(e));
            }
        });

        unitPanel.addMouseWheelListener(e ->
                editorInteraction.mouseWheelMoved(toMouseEventData(e))
        );

        unitPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                editorInteraction.keyPressed(toKeyEventData(e));
            }
            @Override
            public void keyReleased(KeyEvent e) {
                editorInteraction.keyReleased(toKeyEventData(e));
            }
            @Override
            public void keyTyped(KeyEvent e) {
                editorInteraction.keyTyped(toKeyEventData(e));
            }
        });
    }

    // 转换工具方法
    private MouseEventData toMouseEventData(MouseEvent e) {
        MouseEventData data = new MouseEventData();
        data.setId(e.getID());
        data.setX(e.getX());
        data.setY(e.getY());
        data.setClickCount(e.getClickCount());
        data.setButton(e.getButton());
        data.setModifiersEx(e.getModifiersEx());
        data.setPopupTrigger(e.isPopupTrigger());
        data.setWhen(e.getWhen());
        return data;
    }
    private MouseEventData toMouseEventData(MouseWheelEvent e) {
        MouseEventData data = toMouseEventData((MouseEvent)e);
        data.setId(MouseEventData.MOUSE_WHEEL);
        return data;
    }
    private KeyEventData toKeyEventData(KeyEvent e) {
        KeyEventData data = new KeyEventData();
        data.setId(e.getID());
        data.setKeyCode(e.getKeyCode());
        data.setKeyChar(e.getKeyChar());
        data.setModifiersEx(e.getModifiersEx());
        data.setWhen(e.getWhen());
        return data;
    }

    private void build() {
        root = editorInteraction.getRoot();
        treeRoot = editorInteraction.getTreeRoot();

        treeModel = new DefaultTreeModel(treeRoot.getTreeNode(), false);
        tableModel = createTableModel((IPropertySource) treeRoot.getModel());

        treeNavigator.setModel(treeModel);
        contentProvider.preBuildRoot();

        root.refresh();
        treeRoot.refresh();
        contentProvider.postBuildRoot();

        postBuild();
        updateRootFigure(root.getFigure());
    }

    private TreeSelectionListener treeSelectionListener = e -> selectTreeNode();

    private boolean isPopupTrigger(MouseEvent evt) {
        return evt.isPopupTrigger() || evt.getButton() == MouseEvent.BUTTON3;
    }

    private void postBuild() {
        treeNavigator.addTreeSelectionListener(treeSelectionListener);

        treeNavigator.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                if (isPopupTrigger(evt)) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeNavigator.getLastSelectedPathComponent();
                    if (node == null)
                        return;

                    editorInteraction.getTreePopupMenu(node.getUserObject()).show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        treeNavigator.setCellRenderer(new DefaultTreeCellRenderer() {
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                          boolean sel, boolean expanded, boolean leaf, int row,
                                                          boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                AbstractTreeEditPart treePart = (AbstractTreeEditPart) ((DefaultMutableTreeNode) value).getUserObject();
                setText(treePart.getText());
                setIcon(treePart.getImage());
                return this;
            }
        });

        treeNavigator.expandPath(new TreePath(treeRoot.getTreeNode()));
    }

    private boolean isRefreshAllowed() {
        if (editorInteraction.isInProcessing() || saving.get())
            return false;

        return getFile() != null && getFile().isValid();
    }

    //Triggered by PSI change, i.e. rename method or class
    public void psiChanged() {
        if (!isRefreshAllowed()) return;

        // 在 read action 中安全读取 PSI 内容
        ApplicationManager.getApplication().runReadAction(() -> {
            reloadDiagram(loadDiagramFromPsi());
        });
    }

    //Triggered by VFS
    public void virtualFileChanged() {
        if (isRefreshAllowed()) return;
        
        WriteAction.run(() -> {
            reloadDiagram(loadDiagramFromVfs());
        });
    }

    private void reloadDiagram(T diagram) {
        if(diagram == null) return;

        try {
            editorInteraction.setModel(diagram);
            build();
            selectModel(getModel());
            getCommandStack().clear();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private T loadDiagram() {
        //Check PSI for latest change 
        T diagram = loadDiagramFromPsi();
        return diagram == null ? loadDiagramFromVfs() : diagram;
    }

    private T loadDiagramFromVfs() {
        try {
            getFile().refresh(false, false);
            return contentProvider.getContent();
        } catch (Throwable e) {
            try {
                Messages.showErrorDialog("Error: \n" + e.getMessage() + "\n" + VfsUtilCore.loadText(contentProvider.getFile()), "Can not load content change");
            } catch (Throwable e1) {
            }
            throw new IllegalArgumentException("Can not load content", e);
        }
    }

    private T loadDiagramFromPsi() {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(getFile());
        if(psiFile == null) return null;

        Document doc = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (doc == null) return null;

        String latestContent = doc.getText();

        try {
            return contentProvider.convert(latestContent);
        } catch (Exception e) {
            Messages.showErrorDialog("Error: \n" + e.getMessage() + "\n" + latestContent, "Model Loading From PSI Failed");
            throw new IllegalArgumentException(e);
        }
    }

    public void register(ContentChangeListener listener) {
        editorInteraction.register(listener);
    }

    public void save(T model) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                saving.set(true);

                String contentStr = contentProvider.convert(model);
                //Old plugin
                if (contentStr == null) {
                    contentProvider.saveContent();
                } else {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(getFile());
                    Document doc = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                    if (doc == null)
                        contentProvider.saveContent();
                    else {
                        doc.setText(contentStr);
                        PsiDocumentManager.getInstance(project).commitDocument(doc);
                    }
                }

                saving.set(false);
            } catch (Throwable e) {
                saving.set(false);
                throw new IllegalStateException("Can not save change", e);
            }
        });
    }

    private Figure rootFigure;
    private Figure feedbackFigure;

    public void updateRootFigure(Figure rootFigure) {
        if (editorInteraction.isInProcessing())
            return;

        this.rootFigure = rootFigure;
        Dimension size = unitPanel.getPreferredSize();
        innerDiagramPane.getVerticalScrollBar().setMaximum(size.height);
        innerDiagramPane.getHorizontalScrollBar().setMaximum(size.width);
        repaint();
    }

    @Override
    public void updateSelectedFigure(Figure selectedFigure) {
        lastSelected = selectedFigure;
        refreshVisual();
    }

    @Override
    public void updateSelectedModel(Object model) {
        updatePropertySelection(model);
        updateTreeSelection(model);
    }

    @Override
    public void updateFeedbackFigure(Figure feedbackFigure) {
        this.feedbackFigure = feedbackFigure;
    }

    private void updatePropertySelection(Object model) {
        if (model == null)
            return;

        if (!(model instanceof IPropertySource)) {
            tableModel = null;
            tableProperties.setVisible(false);
            return;
        }

//        if(tableModel != null && tableModel.isSame((IPropertySource) model))
//            return;

        tableModel = createTableModel((IPropertySource) model);
        tableProperties.setVisible(true);
        tableProperties.setModel(tableModel);
        tableProperties.setDefaultRenderer(Object.class, new SimpleTableRenderer(tableModel));
        tableProperties.getColumnModel().getColumn(1).setCellEditor(new SimpleTableCellEditor(tableModel));
    }

    private PropertyTableModel createTableModel(IPropertySource model) {
        return new PropertyTableModel(model, editorInteraction);
    }

    private void updateTreeSelection(Object model) {
        triggedByFigure = true;
        AbstractTreeEditPart treePart = (AbstractTreeEditPart) treeRoot.findEditPart(model);
        if (treePart == null) {
            treeNavigator.clearSelection();
            return;
        }

        TreeNode selected = treePart.getTreeNode();
        expandSelected(new TreePath(treeNavigator.getModel().getRoot()), selected);
        treeNavigator.scrollPathToVisible(new TreePath(selected));
    }

    private void adjustEditPanel() {
        if (lastSelected == null)
            return;

        if (lastSelected == root.getFigure())
            return;

        root.getFigure().layout();

        Point pos = lastSelected.getLocation();
        lastSelected.translateToAbsolute(pos);

        adjust(innerDiagramPane.getVerticalScrollBar(), pos.y, lastSelected.getHeight());
        adjust(innerDiagramPane.getHorizontalScrollBar(), pos.x, lastSelected.getWidth());
    }

    private boolean triggedByFigure = false;

    public void selectTopLevelElement(String idField, String name) {
        if (idField == null || name == null)
            return;

        for (Object child : treeRoot.getChildren()) {
            Object model = ((AbstractTreeEditPart) child).getModel();
            if (model instanceof IPropertySource)
                if (name.equals(((IPropertySource) model).getPropertyValue(idField)))
                    selectModel(model);
        }
    }

    public void selectModel(Object selectedNode) {
        editorInteraction.selectModel(selectedNode);
        adjustEditPanel();
    }

    private void selectTreeNode() {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) treeNavigator.getLastSelectedPathComponent();
        if (treeNode == null)
            return;

        if (editorInteraction.isInProcessing())
            return;

        if (triggedByFigure) {
            triggedByFigure = false;
            return;
        }

        AbstractTreeEditPart treePart = (AbstractTreeEditPart) treeNode.getUserObject();
        editorInteraction.selectTreeModel(treePart.getModel());
        adjustEditPanel();
    }

    private boolean expandSelected(TreePath parent, TreeNode selectedNode) {
        // Traverse children
        TreeNode node = (TreeNode) parent.getLastPathComponent();

        if (selectedNode == node) {
            treeNavigator.setSelectionPath(parent);
            return true;
        }

        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                if (expandSelected(path, selectedNode)) {
                    // Expansion or collapse must be done bottom-up
                    treeNavigator.expandPath(parent);
                    return true;
                }
            }
        }
        return false;
    }

    private void adjust(JScrollBar scrollBar, int start, int length) {
        if (scrollBar.getValue() > start || scrollBar.getValue() + scrollBar.getVisibleAmount() < start + length)
            scrollBar.setValue(start - 100);
    }

    public void showContextMenu(int x, int y, JPopupMenu menu) {
        menu.show(unitPanel, x, y);
    }

    @Override
    public void refreshVisual() {
        repaint();
        unitPanel.grabFocus();
    }

    public void execute(Command command) {
        editorInteraction.execute(command);
    }

    public void undo() {
        editorInteraction.undo();
    }

    public void redo() {
        editorInteraction.redo();
    }

    private class UnitPanel extends JPanel {
        @Override
        protected void paintChildren(Graphics g) {
            if (editorInteraction.isInProcessing())
                return;

            root.getFigure().paint(g);
            if(feedbackFigure != null && feedbackFigure instanceof Connection)
                ((Connection)feedbackFigure).paintCreationFeedback(g);
        }

        @Override
        public Dimension getPreferredSize() {
            if (root == null)
                return new Dimension(500, 800);

            Dimension size = rootFigure.getPreferredSize();
            rootFigure.setSize(size);
            size.height += 100;
            return size;
        }
    }

    /**
     * Accessors for extension
     **/
    public VirtualFile getFile() {
        return contentProvider.getFile();
    }

    public T getModel() {
        return editorInteraction.getModel();
    }

    public JPanel getUnitPanel() {
        return unitPanel;
    }

    public AbstractGraphicalEditPart getRoot() {
        return root;
    }

    public AbstractTreeEditPart getTreeRoot() {
        return treeRoot;
    }

    public Tree getTreeNavigator() {
        return treeNavigator;
    }

    public JBTable getTableProperties() {
        return tableProperties;
    }

    public CommandStack getCommandStack() {
        return editorInteraction.getCommandStack();
    }

    public CommandExecutor getCommandExecutor() {
        return editorInteraction;
    }
}