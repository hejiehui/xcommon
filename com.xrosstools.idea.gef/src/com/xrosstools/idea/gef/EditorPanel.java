package com.xrosstools.idea.gef;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.Tree;
import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.actions.CommandExecutor;
import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.CommandStack;
import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.figures.Endpoint;
import com.xrosstools.idea.gef.figures.Figure;
import com.xrosstools.idea.gef.parts.*;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class EditorPanel<T extends IPropertySource> extends JPanel implements CommandExecutor {
    private JBSplitter mainPane;
    private JBSplitter diagramPane;
    private Tree treeNavigator;
    private JBTable tableProperties;

    private JScrollPane innerDiagramPane;
    private UnitPanel unitPanel;
    private AbstractGraphicalEditPart root;
    private AbstractTreeEditPart treeRoot;

    private AtomicReference<T> diagramRef = new AtomicReference<>();
    private ContextMenuProvider contextMenuBuilder;
    private ContextMenuProvider outlineContextMenuProvider;
    private Extension extension;

    private Point lastHit;
    private DefaultTreeModel treeModel;
    private PropertyTableModel tableModel;
    private Figure lastSelected;
    private Figure lastHover;
    private Point lastHoverLocation;
    private boolean isRightButton;

    private Object newModel;
    private AbstractGraphicalEditPart sourcePart;

    private PanelContentProvider<T> contentProvider;
    private List<ContentChangeListener<T>> listeners = new ArrayList<>();

    private CommandStack commandStack = new CommandStack();
    private AtomicBoolean inProcessing = new AtomicBoolean(false);

    private AtomicBoolean saving = new AtomicBoolean(false);

    public EditorPanel(PanelContentProvider<T> contentProvider) throws Exception {
        this.contentProvider = contentProvider;
        contentProvider.setEditorPanel(this);
        diagramRef.set(contentProvider.getContent());
        contextMenuBuilder = contentProvider.getContextMenuProvider();
        contextMenuBuilder.setExecutor(this);
        outlineContextMenuProvider = contentProvider.getOutlineContextMenuProvider();
        outlineContextMenuProvider.setExecutor(this);

        extension = getExtension();

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
            if(component instanceof JButton && ((JButton) component).getActionListeners()[0] instanceof Action) {
                ((Action) ((JButton) component).getActionListeners()[0]).setExecutor(this);
            }
        }

        return palette;
    }

    private JComponent createToolbar() {
        ActionManager actionManager = ActionManager.getInstance();
        ActionGroup actionGroup = contentProvider.createToolbar();
        createUndoRedo(actionGroup);

        extension.extendToolbar(actionGroup);

        ActionToolbar toolbar = actionManager.createActionToolbar("XrossToolsToolbar", actionGroup, true);
        return toolbar.getComponent();
    }

    private void createUndoRedo(ActionGroup group) {
        DefaultActionGroup actionGroup = (DefaultActionGroup)group;
        if(actionGroup.getChildrenCount() > 0) {
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
        tableModel = createTableModel(diagramRef.get());
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

    private Extension getExtension() {
        ExtensionPointName<Extension> ep = ExtensionPointName.create("com.xrosstools.idea.gef.xrossExtension");
        Extension extension = ep.getExtensionList().size() == 1 ? ep.getExtensionList().get(0) : new ExtensionAdapter();

        extension.setEditPanel(this);

        return extension;
    }

    private void reset() {
        inProcessing.set(false);
        gotoNext(ready);
    }

    public void createConnection(Object connModel) {
        newModel = connModel;
        gotoNext(connectionCreated);
    }

    public void createModel(Object model) {
        newModel = model;
        gotoNext(modelCreated);
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    private void build() {
        contentChanged(diagramRef.get());

        EditContext editContext = new EditContext(this);
        EditPartFactory editPartFactory = contentProvider.createEditPartFactory();
        EditPartFactory treeEditPartFactory = contentProvider.createTreePartFactory();

        root = (AbstractGraphicalEditPart) editPartFactory.createEditPart(editContext, null, diagramRef.get());
        root.activate();
        treeRoot = (AbstractTreeEditPart) treeEditPartFactory.createEditPart(editContext, null, diagramRef.get());
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

        treeNavigator.setCellRenderer(new DefaultTreeCellRenderer() {
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                          boolean sel, boolean expanded, boolean leaf, int row,
                                                          boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                AbstractTreeEditPart treePart = (AbstractTreeEditPart)((DefaultMutableTreeNode) value).getUserObject();
                setText(treePart.getText());
                setIcon(treePart.getImage());
                return this;
            }
        });

        treeNavigator.expandPath(new TreePath(treeRoot.getTreeNode()));
    }

    private void refresh() {
        root.refresh();
        treeRoot.refresh();
    }

    public void contentsChanged() {
        if(inProcessing.get() || saving.get())
            return;

        try {
            contentProvider.getFile().refresh(false, true);
            diagramRef.set(contentProvider.getContent());
            build();
            selectModel(diagramRef.get());
            commandStack.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void register(ContentChangeListener listener) {
        listeners.add(listener);
    }

    public void contentChanged(T content) {
        for(ContentChangeListener<T> listener: listeners)
            listener.contentChanged(content);
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
        if(f == null || f == root.getFigure())
            unitPanel.setToolTipText(null);
        else{
            unitPanel.setToolTipText(f.getToolTipText());
        }
    }

    private Figure findFigureAt(Point location) {
        Figure rootFigure = root.getFigure();
        Figure selected = rootFigure.selectFigureAt(location.x, location.y);
        return selected == null ? rootFigure : selected;
    }

    private void updateHover(Figure underPoint, Point location, Command cmd, boolean showInsertionFeedback) {
        underPoint = underPoint == null ? root.getFigure() : underPoint;

        if(lastHover != null && lastHover != underPoint) {
            lastHover.getPart().getContentPane().setInsertionPoint(null);
        }

        if(cmd != null && showInsertionFeedback) {
            Point localLocation = toLocalPoint(underPoint, location);
            underPoint.getPart().getContentPane().setInsertionPoint(localLocation);
        }

        lastHoverLocation = location;
        unitPanel.repaint();
        lastHover = underPoint;
    }

    private void clearHover() {
        if(lastHover == null)
            return;

        lastHover.getPart().getContentPane().setInsertionPoint(null);
        unitPanel.repaint();
        lastHover = null;
        lastHoverLocation = null;
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

    private void updatePropertySelection(Object model) {
        if(model == null)
            return;

        if(!(model instanceof IPropertySource)) {
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
        return new PropertyTableModel(model, this);
    }

    public void updateTreeSelection(Object model) {
        triggedByFigure = true;
        AbstractTreeEditPart treePart = (AbstractTreeEditPart)treeRoot.findEditPart(model);
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

    public void selectModel(Object selectedNode) {
        Figure selected = root.findFigure(selectedNode);
        selected.setSelected(true);
        updateFigureSelection(selected);
        updateTreeSelection(selectedNode);
        updatePropertySelection(selectedNode);

        if(selected == null) {
            gotoNext(ready);
        }else {
            lastHit = null;
            gotoNext(figureSelected);
        }

        adjustEditPanel();
    }

    private void selectTreeNode() {
        if(inProcessing.get())
            return;

        if(triggedByFigure) {
            triggedByFigure = false;
            return;
        }

        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)treeNavigator.getLastSelectedPathComponent();
        if(treeNode == null)
            return;

        AbstractTreeEditPart treePart = (AbstractTreeEditPart)treeNode.getUserObject();

        Figure selected = root.findFigure(treePart.getModel());
        updateFigureSelection(selected);
        updatePropertySelection(treePart.getModel());

        if(selected == null) {
            gotoNext(ready);
        }else {
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
            Endpoint endpoint = (Endpoint)f;
            if(endpoint.isConnectionSourceEndpoint())
                gotoNext(sourceEndpointSelected);
            else if(endpoint.isConnectionTargetEndpoint())
                gotoNext(targetEndpointSelected);
            else if(endpoint.isConnectionAdjusterEndpoint())
                gotoNext(adjusterEndpointSelected);
        }
    }

    private boolean expandSelected(TreePath parent, TreeNode selectedNode) {
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();

        if(selectedNode == node) {
            treeNavigator.setSelectionPath(parent);
            return true;
        }

        if (node.getChildCount() >= 0) {
            for(Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                if(expandSelected(path, selectedNode)) {
                    // Expansion or collapse must be done bottom-up
                    treeNavigator.expandPath(parent);
                    return true;
                }
            }
        }
        return false;
    }

    private void adjust(JScrollBar scrollBar, int start, int length) {
        if(scrollBar.getValue() > start || scrollBar.getValue() + scrollBar.getVisibleAmount() < start + length)
            scrollBar.setValue(start - 100);
    }

    private void showContextMenu(int x, int y) {
        contextMenuBuilder.buildDisplayMenu(lastSelected.getPart()).show(unitPanel, x, y);
    }

    public void refreshVisual() {
        updateVisual();
        unitPanel.grabFocus();
    }

    public void execute(Command command) {
        if(command == null || command.canExecute() == false)
            return;

        Object model = newModel;
        if(model == null)
            model = lastSelected == null ? null : lastSelected.getPart().getModel();
        inProcessing.set(true);
        commandStack.execute(command, model);

        if(newModel != null)
            newModel = null;

        postExecute(model);
    }

    private void postExecute(Object model) {
        refresh();
        save();

        AbstractGraphicalEditPart part = root.findEditPart(model);
        model = part == null ? diagramRef.get() : model;

        Figure selected = root.findFigure(model);
        if (selected == null || !selected.isSelectable())
            model = diagramRef.get();

        selectModel(model);
        clearHover();
        inProcessing.set(false);
        refreshVisual();
    }

    private void undo() {
        inProcessing.set(true);
        commandStack.undo();
        postExecute(commandStack.getCurModel());
    }

    private void redo() {
        inProcessing.set(true);
        commandStack.redo();
        postExecute(commandStack.getCurModel());
    }

    private void updateVisual() {
        if(inProcessing.get())
            return;

        Dimension size = unitPanel.getPreferredSize();
        innerDiagramPane.getVerticalScrollBar().setMaximum(size.height);
        innerDiagramPane.getHorizontalScrollBar().setMaximum(size.width);
        repaint();
    }

    private class UnitPanel extends JPanel {
        @Override
        protected void paintChildren(Graphics g) {
            if(inProcessing.get())
                return;

            root.getFigure().paint(g);
            curHandle.paint(g);
        }

        @Override
        public Dimension getPreferredSize() {
            if(root == null)
                return new Dimension(500, 800);

            Dimension size = root.getFigure().getPreferredSize();
            root.getFigure().setSize(size);
            size.height += 100;
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
            isRightButton = e.getButton() == MouseEvent.BUTTON3;
            gotoNext(figureSelected);
        }
    };

    private Point toLocalPoint(Figure target, Point location) {
        Point point = new Point(location);
        Figure contentPane = target.getPart().getContentPane();
        contentPane.translateToRelative(point);
        contentPane.translateFromParent(point);
        return point;
    }
    private InteractionHandle figureSelected = new InteractionHandle("figureSelected") {
        private boolean moved;
        private Point delta;

        private boolean isApplicable() {
            if(lastSelected.getPart().getModel() == diagramRef.get())
                return false;

            if(lastSelected instanceof Connection)
                return false;

            return true;
        }
        private boolean isAddCommand(Point p, Figure underPoint) {
            Point pos = new Point(p);
            lastSelected.translateToRelative(pos);
            return !lastSelected.containsPoint(pos) && underPoint.getPart() != lastSelected.getPart().getParent() && underPoint != lastSelected;
        }

        private Command getCommand(boolean isAdd, Figure target, Point p) {
            AbstractGraphicalEditPart part = lastSelected.getPart();
            AbstractGraphicalEditPart parentPart = target.getPart();
            EditPolicy policy = parentPart.getEditPolicy();

            Point localPoint = toLocalPoint(parentPart.getFigure(), p);
            if(policy == null)
                return null;

            Rectangle constrain = new Rectangle(localPoint.x + delta.x, localPoint.y + delta.y, lastSelected.getWidth(), lastSelected.getHeight());
            return isAdd ? policy.getAddCommand(part, constrain) : policy.getMoveCommand(part, constrain);
        }

        private Figure getTarget(boolean isAdd, Figure underPoint) {
            return isAdd ? underPoint.getPart().getFigure() : ((AbstractGraphicalEditPart)lastSelected.getPart().getParent()).getFigure();
        }

        private boolean showInsertionFeedback(Figure target, Command cmd) {
            return cmd != null && target.getPart().getEditPolicy().isInsertable(cmd);
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
            isRightButton = e.getButton() == MouseEvent.BUTTON3;
            enter();
        }

        public void mouseDragged(MouseEvent e) {
            Point p = e.getPoint();
            if(isRightButton || !isApplicable())
                return;

            Figure underPoint = findFigureAt(p);
            boolean isAdd = isAddCommand(p, underPoint);
            Figure target = getTarget(isAdd, underPoint);
            Command cmd = getCommand(isAdd, target, p);
            moved = cmd != null;

            updateHover(target, p, cmd, showInsertionFeedback(target, cmd));
        }

        public void mouseReleased(MouseEvent e) {
            if(isPopupTrigger(e))
                showContextMenu(e.getX(), e.getY());
            else if(moved && lastHover != null && isApplicable()) {
                Point p = e.getPoint();
                Figure underPoint = findFigureAt(p);
                boolean isAdd = isAddCommand(p, underPoint);
                Figure target = getTarget(isAdd, underPoint);
                Command cmd = getCommand(isAdd, target, p);
                clearHover();
                execute(cmd);
            }

            moved = false;
        }

        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount() == 2) {
                lastSelected.getPart().performAction();
            }
        }

        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_DELETE) {
                EditPolicy policy = lastSelected.getPart().getEditPolicy();
                if(policy == null)
                    return;

                Command deleteCmd = policy.getDeleteCommand();
                if (deleteCmd == null)
                    return;

                execute(deleteCmd);
            }
        }

        public void paint(Graphics g) {
            if(!moved)
                return;

            lastHoverLocation.translate(delta.x, delta.y);
            lastSelected.paintDragFeedback(g, lastHoverLocation);
        }
    };

    private InteractionHandle modelCreated = new InteractionHandle("modelCreated") {
        private Command getCreateCommand(Figure underPoint, Point p) {
            EditPolicy policy = underPoint.getPart().getEditPolicy();
            if(policy == null) return null;

            final Point point = toLocalPoint(underPoint, p);
            return policy.getCreateCommand(newModel, point);
        }

        private boolean showInsertionFeedback(Figure underPoint, Command cmd) {
            AbstractGraphicalEditPart parentPart = underPoint.getPart();
            return parentPart.getEditPolicy() != null && parentPart.getEditPolicy().isInsertable(cmd);
        }

        public void mouseMoved(MouseEvent e) {
            Point p = e.getPoint();
            Figure underPoint = findFigureAt(p);
            Command cmd = getCreateCommand(underPoint, p);
            updateHover(underPoint, p, cmd, showInsertionFeedback(underPoint, cmd));
        }

        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            Figure underPoint = findFigureAt(p);
            Command createCommand = getCreateCommand(underPoint, p);
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
            showSourceFeedback(f, isSelectableSource(f));
        }

        public void mousePressed(MouseEvent e) {
            eraseSourceFeedback();
            Figure f = findFigureAt(e.getPoint());
            if(isSelectableSource(f)) {
                sourcePart = f.getPart();
                gotoNext(sourceSelected);
            }else {
                gotoNext(ready);
            }
        }

        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                eraseSourceFeedback();
                gotoNext(ready);
            }
        }

        private boolean isSelectableSource(Figure f) {
            return f.getPart().getEditPolicy() == null ? false : f.getPart().getEditPolicy().isSelectableSource(newModel);
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
            lastHoverLocation = e.getPoint();
            Figure f = findFigureAt(lastHoverLocation);
            boolean isSelectableTarget = getCommand(f) != null;
            conn.relocateTargetFeedback(isSelectableTarget ? f : new Point(lastHoverLocation));
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

        private Command getCommand(Figure underPoint) {
            EditPolicy policy = underPoint.getPart().getEditPolicy();
            if(policy == null) return null;

            return policy.getCreateConnectionCommand(newModel, sourcePart);
        }
    };

    private Figure lastSelectableTarget;

    private void showTargetFeedback(Figure f, boolean isSelectableTarget) {
        if(f == null || f == lastSelectableTarget) {
            repaint();
            return;
        }

        eraseTargetFeedback();
        if(isSelectableTarget) {
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
            lastHoverLocation = e.getPoint();
            Figure f = findFigureAt(e.getPoint());
            boolean isSelectableSource = getCommand(f) != null;
            endpoint.getParentConnection().relocateSourceFeedback(isSelectableSource ? f : lastHoverLocation);
            showSourceFeedback(f, isSelectableSource);
        }

        public void mouseReleased(MouseEvent e) {
            Figure f = findFigureAt(e.getPoint());
            endpoint.getParentConnection().clearFeedback();
            eraseSourceFeedback();
            execute(getCommand(f));
            gotoNext(ready);
        }

        private Command getCommand(Figure underPoint) {
            EditPolicy policy = underPoint.getPart().getEditPolicy();
            if(policy == null) return null;

            Connection connection = ((Endpoint)lastSelected).getParentConnection();
            return policy.getReconnectSourceCommand(connection.getConnectionPart());
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
            lastHoverLocation = e.getPoint();
            Figure f = findFigureAt(e.getPoint());
            boolean isSelectableTarget = getCommand(f) != null;
            endpoint.getParentConnection().relocateTargetFeedback(isSelectableTarget ? f : new Point(lastHoverLocation));
            showTargetFeedback(f, isSelectableTarget);
        }

        public void mouseReleased(MouseEvent e) {
            Figure f = findFigureAt(e.getPoint());
            endpoint.getParentConnection().clearFeedback();
            eraseTargetFeedback();
            execute(getCommand(f));
            gotoNext(ready);
        }

        private Command getCommand(Figure underPoint) {
            EditPolicy policy = underPoint.getPart().getEditPolicy();
            if(policy == null) return null;

            Connection connection = ((Endpoint) lastSelected).getParentConnection();
            return policy.getReconnectTargetCommand(connection.getConnectionPart());
        }
    };

    private InteractionHandle adjusterEndpointSelected = new InteractionHandle("adjusterEndpointSelected") {
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
            endpoint.setAdjustment(e.getPoint());
            repaint();
        }

        public void mouseReleased(MouseEvent e) {
            endpoint.setAdjustment(new Point(e.getPoint()));
            execute(getCommand());
            gotoNext(ready);
        }

        private Command getCommand() {
            EditPolicy policy = endpoint.getPart().getEditPolicy();
            if(policy == null) return null;

            Connection connection = ((Endpoint) lastSelected).getParentConnection();
            return policy.getAdjustConnectionCommand(connection.getConnectionPart());
        }
    };

    private InteractionHandle curHandle = ready;

    /** Accessors for extension **/
    public T getModel() {
        return diagramRef.get();
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
}