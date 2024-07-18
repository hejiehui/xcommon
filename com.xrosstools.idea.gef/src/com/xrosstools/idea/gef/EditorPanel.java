package com.xrosstools.idea.gef;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.Tree;
import com.xrosstools.idea.gef.actions.CommandExecutor;
import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.CommandStack;
import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.figures.Endpoint;
import com.xrosstools.idea.gef.figures.Figure;
import com.xrosstools.idea.gef.figures.ToolbarLayout;
import com.xrosstools.idea.gef.parts.AbstractGraphicalEditPart;
import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;
import com.xrosstools.idea.gef.parts.EditContext;
import com.xrosstools.idea.gef.parts.EditPartFactory;
import com.xrosstools.idea.gef.util.IPropertySource;
import com.xrosstools.idea.gef.util.PropertyTableModel;
import com.xrosstools.idea.gef.util.SimpleTableCellEditor;
import com.xrosstools.idea.gef.util.SimpleTableRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

public class EditorPanel<T extends IPropertySource> extends JPanel implements CommandExecutor {
    private JBSplitter mainPane;
    private JBSplitter diagramPane;
    private Tree treeNavigator;
    private JBTable tableProperties;

    private JScrollPane innerDiagramPane;
    private UnitPanel unitPanel;
    private AbstractGraphicalEditPart root;
    private AbstractTreeEditPart treeRoot;

    private T diagram;
    private ContextMenuProvider contextMenuBuilder;
    private ContextMenuProvider outlineContextMenuProvider;

    private Point lastHit;
    private DefaultTreeModel treeModel;
    private PropertyTableModel tableModel;
    private Figure lastSelected;
    private Figure lastHover;
    private Point lastHoverlocation;

    private Object newModel;
    private AbstractGraphicalEditPart sourcePart;

    private PanelContentProvider contentProvider;

    private CommandStack commandStack = new CommandStack();
    private boolean inProcessing;

    private AtomicBoolean saving = new AtomicBoolean(false);

    public EditorPanel(PanelContentProvider<T> contentProvider) throws Exception {
        this.contentProvider = contentProvider;
        contentProvider.setEditorPanel(this);
        diagram = contentProvider.getContent();
        contextMenuBuilder = contentProvider.getContextMenuProvider();
        contextMenuBuilder.setExecutor(this);
        outlineContextMenuProvider = contentProvider.getOutlineContextMenuProvider();
        outlineContextMenuProvider.setExecutor(this);

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
        GridLayout layout = new GridLayout(0, 1, 10,0);
        palette.setLayout(layout);

        palette.add(createResetButton());
        contentProvider.buildPalette(palette);
        return palette;
    }

    private JComponent createToolbar() {
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup actionGroup = contentProvider.createToolbar();
        createUndoRedo(actionGroup);
        ActionToolbar toolbar = actionManager.createActionToolbar("XrossToolsToolbar", actionGroup, true);
        return toolbar.getComponent();
    }

    private void createUndoRedo(ActionGroup group) {
        DefaultActionGroup actionGroup = (DefaultActionGroup)group;
        if(((DefaultActionGroup) group).getChildrenCount() > 0) {
            actionGroup.addSeparator();
        }

        actionGroup.add(new AnAction("Undo", "Undo", GefIcons.Undo) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                undo();
            }

            @Override
            public void update(AnActionEvent e) {
                super.update(e);
                Presentation presentation = e.getPresentation();
                presentation.setEnabled(commandStack.canUndo());
                if(presentation.isEnabled())
                    presentation.setText("Undo " + commandStack.getUndoCommandLabel());
            }
        });

        actionGroup.add(new AnAction("Redo", "Redo", GefIcons.Redo) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                redo();
            }

            @Override
            public void update(AnActionEvent e) {
                super.update(e);
                Presentation presentation = e.getPresentation();
                presentation.setEnabled(commandStack.canRedo());
                if(presentation.isEnabled())
                    presentation.setText("Redo " + commandStack.getRedoCommandLabel());
            }
        });

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
        tableModel = createTableModel(diagram);
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
        btn.setPreferredSize(new Dimension(100, 50));
        btn.setContentAreaFilled(false);
        btn.addActionListener(e -> reset());
        return btn;
    }

    private void reset(){
        inProcessing = false;
        gotoNext(ready);
    }

    public void createConnection(Object connModel){
        newModel = connModel;
        gotoNext(connectionCreated);
    }

    public void createModel(Object model){
        newModel = model;
        gotoNext(modelCreated);
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    private void build() {
        EditContext editContext = new EditContext(this);
        EditPartFactory editPartFactory = contentProvider.createEditPartFactory();
        EditPartFactory treeEditPartFactory = contentProvider.createTreePartFactory();

        root = (AbstractGraphicalEditPart) editPartFactory.createEditPart(editContext,null, diagram);
        root.activate();
        treeRoot = (AbstractTreeEditPart) treeEditPartFactory.createEditPart(editContext,null, diagram);
        treeRoot.activate();

        treeModel = new DefaultTreeModel(treeRoot.getTreeNode(), false);
        tableModel = createTableModel((IPropertySource) treeRoot.getModel());

        treeNavigator.setModel(treeModel);
        contentProvider.preBuildRoot();

        root.refresh();
        treeRoot.refresh();
        contentProvider.postBuildRoot();

        postBuild();
        updateVisual();
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
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeNavigator.getLastSelectedPathComponent();
                    if(node == null)
                        return;

                    outlineContextMenuProvider.buildDisplayMenu(node.getUserObject()).show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        treeNavigator.setCellRenderer(new DefaultTreeCellRenderer(){
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                          boolean sel, boolean expanded, boolean leaf, int row,
                                                          boolean hasFocus){
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                AbstractTreeEditPart treePart = (AbstractTreeEditPart)((DefaultMutableTreeNode)value).getUserObject();
                setText(treePart.getText());
                setIcon(treePart.getImage());
                return this;
            }
        });

        treeNavigator.expandPath(new TreePath(treeRoot.getTreeNode()));
    }

    public void refresh() {
        root.refresh();
        treeRoot.refresh();
        refreshVisual();
    }

    public void contentsChanged() {
        if(inProcessing || saving.get())
            return;

        try {
            contentProvider.getFile().refresh(false, true);
            diagram = (T)contentProvider.getContent();
            build();
            selectModel(diagram);
            commandStack.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save() {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                saving.set(true);

                contentProvider.saveContent();

                saving.set(false);
            } catch (Throwable e) {
                saving.set(false);
                throw new IllegalStateException("Can not save change", e);
            }
        });
    }

    private void updateTooltip(Point location) {
        Figure f = findFigureAt(location);
        if (f == null || f == root.getFigure())
            unitPanel.setToolTipText(null);
        else {
            unitPanel.setToolTipText(f.getToolTipText());
        }
    }

    private Figure findFigureAt(Point location) {
        Figure rootFigure = root.getFigure();
        Figure selected = rootFigure.selectFigureAt(location.x, location.y);
        return selected == null ? rootFigure : selected;
    }

    private void updateHover(Figure underPoint, Point location, Command cmd) {
        underPoint = underPoint == null ? root.getFigure() : underPoint;

        if(lastHover != null && lastHover != underPoint) {
            lastHover.getPart().getContentPane().setInsertionPoint(null);
        }

        if(cmd != null && underPoint != lastSelected) {
            Figure contentPane = underPoint.getPart().getContentPane();
            boolean toolbarLayout = contentPane.getLayoutManager() instanceof ToolbarLayout;
            //When moving figure arround, we don't show insertion feedback if it is not toobar layout
            //I know it is not very elegant, but I don't have better solution for now
            if(toolbarLayout || !toolbarLayout && newModel != null) {
                Point localLocation = new Point(location);
                contentPane.translateToRelative(localLocation);
                contentPane.translateFromParent(localLocation);
                contentPane.setInsertionPoint(localLocation);
            }
        }

        K:lastHoverlocation = location;
        unitPanel.repaint();
        lastHover = underPoint;
    }

    private void clearHover() {
        if(lastHover == null)
            return;

        lastHover.getPart().getContentPane().setInsertionPoint(null);
        unitPanel.repaint();
        lastHover = null;
        lastHoverlocation = null;
        repaint();
    }

    private void registerListener() {
        unitPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {curHandle.mouseDragged(e);}
            public void mouseMoved(MouseEvent e) {curHandle.mouseMoved(e);}
        });

        unitPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {curHandle.mouseClicked(e);}
            public void mousePressed(MouseEvent e) {curHandle.mousePressed(e);}
            public void mouseReleased(MouseEvent e) {curHandle.mouseReleased(e);}
            public void mouseEntered(MouseEvent e) {curHandle.mouseEntered(e);}
            public void mouseExited(MouseEvent e) {curHandle.mouseExited(e);}
            public void mouseWheelMoved(MouseWheelEvent e){curHandle.mouseWheelMoved(e);}
        });

        unitPanel.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {curHandle.keyTyped(e);}
            public void keyPressed(KeyEvent e) {curHandle.keyPressed(e);}
            public void keyReleased(KeyEvent e) {curHandle.keyReleased(e);}
        });
    }

    private void gotoNext(InteractionHandle next) {
        curHandle.leave();
        next.enter();
        curHandle = next;
    }

    private void updatePropertySelection(Object model){
        if(model == null)
            return;

        if(!(model instanceof IPropertySource)) {
            tableModel = null;
            tableProperties.setVisible(false);
            return;
        }

        if(tableModel != null && tableModel.isSame((IPropertySource) model))
            return;

        tableModel = createTableModel((IPropertySource) model);
        tableProperties.setVisible(true);
        tableProperties.setModel(tableModel);
        tableProperties.setDefaultRenderer(Object.class, new SimpleTableRenderer(tableModel));
        tableProperties.getColumnModel().getColumn(1).setCellEditor(new SimpleTableCellEditor(tableModel));
    }

    private PropertyTableModel createTableModel(IPropertySource model) {
        return new PropertyTableModel(model, this);
    }

    private void updateTreeSelection(Object model) {
        triggedByFigure = true;
        AbstractTreeEditPart treePart = treeRoot.findEditPart(model);
        if(treePart == null) {
            treeNavigator.clearSelection();
            return;
        }

        TreeNode selected = treePart.getTreeNode();
        expandSelected(new TreePath(treeNavigator.getModel().getRoot()), selected);
        treeNavigator.scrollPathToVisible(new TreePath(selected));
    }

    private void updateFigureSelection(Figure selected) {
        if (lastSelected == selected)
            return;

        if (lastSelected != null)
            lastSelected.setSelected(false);

        lastSelected = selected;
        if (lastSelected != null)
            lastSelected.setSelected(true);

        refreshVisual();
    }

    private void adjustEditPanel() {
        if(lastSelected == null)
            return;

        Point pos = lastSelected.getLocation();
        lastSelected.translateToAbsolute(pos);

        adjust(innerDiagramPane.getVerticalScrollBar(), pos.y, lastSelected.getHeight());
        adjust(innerDiagramPane.getHorizontalScrollBar(), pos.x, lastSelected.getWidth());
    }

    private boolean triggedByFigure = false;

    private void selectModel(Object selectedNode) {
        Figure selected = root.getContext().findFigure(selectedNode);
        updateFigureSelection(selected);
        updateTreeSelection(selectedNode);
        updatePropertySelection(selectedNode);

        if(selected == null) {
            gotoNext(ready);
        } else {
            lastHit = null;
            gotoNext(figureSelected);
        }

        adjustEditPanel();
    }

    private void selectTreeNode() {
        if (inProcessing)
            return;

        if(triggedByFigure) {
            triggedByFigure = false;
            return;
        }

        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)treeNavigator.getLastSelectedPathComponent();
        if(treeNode == null)
            return;

        AbstractTreeEditPart treePart = (AbstractTreeEditPart)treeNode.getUserObject();

        Figure selected = treePart.getContext().findFigure(treePart.getModel());
        updateFigureSelection(selected);
        updatePropertySelection(treePart.getModel());

        if(selected == null) {
            gotoNext(ready);
        } else {
            lastHit = null;
            gotoNext(figureSelected);
        }

        adjustEditPanel();
    }

    private void selectFigureAt(Point location) {
        Figure f = findFigureAt(location);
        updateFigureSelection(f);

        Object model = f == null ? null : f.getPart().getModel();
        updateTreeSelection(model);
        updatePropertySelection(model);

        if(f == null) {
            gotoNext(ready);
            return;
        }

        if(f instanceof Endpoint && f.getParent() instanceof Connection) {
            gotoNext(((Endpoint)f).isConnectionSourceEndpoint() ? sourceEndpointSelected : targetEndpointSelected);
        }
    }

    private boolean expandSelected(TreePath parent, TreeNode selectedNode) {
        // Traverse children
        TreeNode node = (TreeNode) parent.getLastPathComponent();

        if(selectedNode == node) {
            treeNavigator.setSelectionPath(parent);
            return true;
        }

        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                if(expandSelected(path, selectedNode)) {
                    // Expansion or collapse must be done bottom-up
                    treeNavigator.expandPath(parent);
                    return  true;
                }
            }
        }
        return false;
    }

    private void adjust(JScrollBar scrollBar, int start, int length ) {
        if (scrollBar.getValue() > start || scrollBar.getValue() + scrollBar.getVisibleAmount() < start + length)
            scrollBar.setValue(start - 100);
    }

    private void showContexMenu(int x, int y) {
        contextMenuBuilder.buildDisplayMenu(lastSelected.getPart()).show(unitPanel, x, y);
    }

    public void refreshVisual() {
        updateVisual();
        unitPanel.grabFocus();
    }

    public void execute(Command command) {
        if(command == null)
            return;

        Object model = newModel;
        if(model == null)
            model = lastSelected == null ? null : lastSelected.getPart().getModel();
        inProcessing = true;
        commandStack.execute(command, model);

        if(newModel != null)
            newModel = null;

        postExecute(model);
    }

    private void postExecute(Object model) {
        save();
        selectModel(model);
        inProcessing = false;
        refresh();
    }

    private void undo() {
        inProcessing = true;
        commandStack.undo();
        postExecute(commandStack.getCurModel());
    }

    private void redo() {
        inProcessing = true;
        commandStack.redo();
        postExecute(commandStack.getCurModel());
    }

    private void updateVisual() {
        if(inProcessing)
            return;

        Dimension size = unitPanel.getPreferredSize();
        innerDiagramPane.getVerticalScrollBar().setMaximum(size.height);
        innerDiagramPane.getHorizontalScrollBar().setMaximum(size.width);
        repaint();
    }

    private class UnitPanel extends JPanel {
        @Override
        protected void paintChildren(Graphics g) {
            root.getFigure().layout();
            root.getFigure().paint(g);
            curHandle.paint(g);
        }

        @Override
        public Dimension getPreferredSize() {
            if(root == null)
                return new Dimension(500,800);

            Dimension size = root.getFigure().getPreferredSize();
            root.getFigure().setSize(size);
            size.height+=100;
            return size;
        }
    }

    private class InteractionHandle extends MouseAdapter implements KeyListener {
        public void keyTyped(KeyEvent e) {}
        public void keyPressed(KeyEvent e) {}
        public void keyReleased(KeyEvent e) {}
        public void enter() {}
        public void leave() {}
        public void paint(Graphics g) {}
        public String id;

        public InteractionHandle(String id) {
            this.id = id;
        }
    }

    private InteractionHandle ready = new InteractionHandle("ready") {
        public void enter() {
            if(lastSelected != null) {
                lastSelected.setSelected(false);
                lastSelected = null;
            }

            newModel = null;
            sourcePart = null;

            clearHover();

            refreshVisual();
        }
        public void mouseMoved(MouseEvent e) {
            updateTooltip(e.getPoint());
        }
        public void mousePressed(MouseEvent e) {
            selectFigureAt(e.getPoint());
            lastHit = new Point(e.getPoint());
            gotoNext(figureSelected);
        }
    };

    private InteractionHandle figureSelected = new InteractionHandle("figureSelected") {
        private boolean moved;
        private Point delta;

        private Command getCommand(Figure underPoint, Point p) {
            p = new Point(p);
            p.translate(delta.x, delta.y);
            if(underPoint.getPart() != lastSelected.getPart().getParent() && underPoint != lastSelected)
                return getAddCommand(underPoint, p);
            else
                return getMoveCommand(underPoint, p);
        }
        private Command getAddCommand(Figure underPoint, Point p) {
            underPoint.getPart().getContentPane().translateToRelative(p);
            AbstractGraphicalEditPart newParentPart = underPoint.getPart();
            if(newParentPart == null || !newParentPart.getContentPane().containsPoint(p))
                return null;

            return newParentPart.getEditPolicy().getAddCommand(lastSelected.getPart(), new Rectangle(p.x, p.y, lastSelected.getWidth(), lastSelected.getHeight()));
        }
        private Command getMoveCommand(Figure underPoint, Point p) {
            lastSelected.translateToRelative(p);
            AbstractGraphicalEditPart parentPart = (AbstractGraphicalEditPart)lastSelected.getPart().getParent();
            if(parentPart == null || !parentPart.getContentPane().containsPoint(p))
                return null;

            return parentPart.getEditPolicy().getMoveCommand(lastSelected.getPart(), new Rectangle(p.x, p.y, lastSelected.getWidth(), lastSelected.getHeight()));
        }
        public void enter() {
            moved = false;
            Point pos = lastSelected.getLocation();
            lastSelected.translateToAbsolute(pos);
            delta = new Point();

            if(lastHit == null)
                return;

            delta.x = pos.x - lastHit.x;
            delta.y = pos.y - lastHit.y;
        }
        public void mouseMoved(MouseEvent e) {
            updateTooltip(e.getPoint());
        }
        public void mousePressed(MouseEvent e) {
            selectFigureAt(e.getPoint());
            lastHit = new Point(e.getPoint());
            enter();
        }
        public void mouseDragged(MouseEvent e) {
            moved = true;
            Point p = e.getPoint();
            if(lastSelected.getPart().getModel() == diagram)
                return;

            if(lastSelected instanceof Connection)
                return;

            Figure underPoint = findFigureAt(p);
            updateHover(underPoint, p, getCommand(underPoint, p));
        }
        public void mouseReleased(MouseEvent e) {
            // Drag and drop
            if (moved && lastSelected != null && lastHover != null) {
                Point p = e.getPoint();
                Figure underPoint = findFigureAt(p);
                Command moveCmd = getCommand(underPoint, p);
                //updateHover(underPoint, p, moveCmd);
                clearHover();
                execute(moveCmd);
            }

            if (isPopupTrigger(e))
                showContexMenu(e.getX(), e.getY());
            moved = false;
        }
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                lastSelected.getPart().performAction();
            }
        }
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_DELETE) {
                Command deleteCmd = lastSelected.getPart().getEditPolicy().getDeleteCommand();
                if(deleteCmd == null)
                    return;

                execute(deleteCmd);
            }
        }
        public void paint(Graphics g) {
            if(!moved)
                return;

            lastHoverlocation.translate(delta.x, delta.y);
            lastSelected.paintDragFeedback(g, lastHoverlocation);
        }
    };

    private InteractionHandle modelCreated = new InteractionHandle("modelCreated") {
        private Command getCreateCommand(Figure underPoint, Point p) {
            p = new Point(p);
            Figure contentPane = underPoint.getPart().getContentPane();
            contentPane.translateToRelative(p);
            contentPane.translateFromParent(p);
            return underPoint.getPart().getEditPolicy().getCreateCommand(newModel, p);
        }
        public void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();
            Figure underPoint = findFigureAt(p);
            updateHover(underPoint, p, getCreateCommand(underPoint, p));
        }
        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            Figure underPoint = findFigureAt(p);
            Command createCommand = getCreateCommand(underPoint, p);
            updateHover(underPoint, p, createCommand);
            execute(createCommand);

            gotoNext(ready);
        }
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
                gotoNext(ready);
        }
    };

    private InteractionHandle connectionCreated = new InteractionHandle("connectionCreated") {
        public void mouseMoved(MouseEvent e) {
            Figure f = findFigureAt(e.getPoint());
            showSourceFeedback(f, f.getPart().getEditPolicy().isSelectableSource(newModel));
        }
        public void mousePressed(MouseEvent e) {
            eraseSourceFeedback();
            Figure f = findFigureAt(e.getPoint());
            if(f.getPart().getEditPolicy().isSelectableSource(newModel)) {
                sourcePart = f.getPart();
                gotoNext(sourceSelected);
            } else {
                gotoNext(ready);
            }
        }
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                eraseSourceFeedback();
                gotoNext(ready);
            }
        }
    };

    private Figure lastSelectableSource;
    private void showSourceFeedback(Figure f, boolean isSelectableSource) {
        if(f == null || f == lastSelectableSource) {
            repaint();
            return;
        }

        eraseSourceFeedback();
        if(isSelectableSource) {
            f.getPart().showSourceFeedback();
            lastSelectableSource = f;
        }
        repaint();
    }

    private void eraseSourceFeedback() {
        if(lastSelectableSource != null) {
            lastSelectableSource.getPart().eraseSourceFeedback();
            lastSelectableSource = null;
        }
        repaint();
    }

    private InteractionHandle sourceSelected = new InteractionHandle("sourceSelected") {
        private Connection conn;
        public void enter() {
            conn = new Connection();
            conn.setSourcePart(sourcePart);
            conn.relocateTargetFeedback(sourcePart.getFigure());
        }
        public void mouseMoved(MouseEvent e) {
            lastHoverlocation = e.getPoint();
            Figure f = findFigureAt(lastHoverlocation);
            boolean isSelectableTarget = getCommand(f) != null;
            conn.relocateTargetFeedback(isSelectableTarget ? f : lastHoverlocation);
            showTargetFeedback(f, isSelectableTarget);
        }
        public void mousePressed(MouseEvent e) {
            Figure f = findFigureAt(e.getPoint());
            eraseTargetFeedback();
            execute(getCommand(f));
            gotoNext(ready);
        }
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                eraseTargetFeedback();
                gotoNext(ready);
            }
        }
        public void paint(Graphics graphics) {
            conn.paintCreationFeedback(graphics);
        }

        private Command getCommand(Figure f) {
            return f.getPart().getEditPolicy().getCreateConnectionCommand(newModel, sourcePart);
        }
    };

    private Figure lastSelectableTarget;

    private void showTargetFeedback(Figure f, boolean isSelectableTarget) {
        if (f == null || f == lastSelectableTarget) {
            repaint();
            return;
        }

        eraseTargetFeedback();
        if (isSelectableTarget) {
            f.getPart().showTargetFeedback();
            lastSelectableTarget = f;
        }
        repaint();
    }

    private void eraseTargetFeedback() {
        if(lastSelectableTarget != null) {
            lastSelectableTarget.getPart().eraseTargetFeedback();
            lastSelectableTarget = null;
            repaint();
        }
    }

    private InteractionHandle sourceEndpointSelected = new InteractionHandle("sourceEndpointSelected") {
        private Endpoint endpoint;
        public void enter() {
            endpoint = (Endpoint)lastSelected;
        }
        public void mousePressed(MouseEvent e) {
            selectFigureAt(e.getPoint());
            if(lastSelected == endpoint)
                return;

            endpoint = null;
            gotoNext(figureSelected);
        }
        public void mouseDragged(MouseEvent e) {
            lastHoverlocation = e.getPoint();
            Figure f = findFigureAt(e.getPoint());
            boolean isSelectableSource = getCommand(f) != null;
            endpoint.getParentConnection().relocateSourceFeedback(isSelectableSource ? f : lastHoverlocation);
            showSourceFeedback(f, isSelectableSource);
        }
        public void mouseReleased(MouseEvent e) {
            Figure f = findFigureAt(e.getPoint());
            endpoint.getParentConnection().clearFeedback();
            eraseSourceFeedback();
            execute(getCommand(f));
            gotoNext(ready);
        }
        private Command getCommand(Figure f) {
            Connection connection = ((Endpoint)lastSelected).getParentConnection();
            return f.getPart().getEditPolicy().getReconnectSourceCommand(connection.getConnectionPart());
        }
    };

    private InteractionHandle targetEndpointSelected = new InteractionHandle("targetEndpointSelected") {
        private Endpoint endpoint;
        public void enter() {
            endpoint = (Endpoint)lastSelected;
        }
        public void mousePressed(MouseEvent e) {
            selectFigureAt(e.getPoint());
            if(lastSelected == endpoint)
                return;

            endpoint = null;
            gotoNext(figureSelected);
        }
        public void mouseDragged(MouseEvent e) {
            lastHoverlocation = e.getPoint();
            Figure f = findFigureAt(e.getPoint());
            boolean isSelectableTarget = getCommand(f) != null;
            endpoint.getParentConnection().relocateTargetFeedback(isSelectableTarget ? f: lastHoverlocation);
            showTargetFeedback(f, isSelectableTarget);
        }
        public void mouseReleased(MouseEvent e) {
            Figure f = findFigureAt(e.getPoint());
            endpoint.getParentConnection().clearFeedback();
            eraseTargetFeedback();
            execute(getCommand(f));
            gotoNext(ready);
        }
        private Command getCommand(Figure f) {
            Connection connection = ((Endpoint)lastSelected).getParentConnection();
            return f.getPart().getEditPolicy().getReconnectTargetCommand(connection.getConnectionPart());
        }
    };
    private InteractionHandle curHandle = ready;
}