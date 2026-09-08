package com.xrosstools.idea.gef.control;

import com.xrosstools.idea.gef.ContentChangeListener;
import com.xrosstools.idea.gef.ContextMenuProvider;
import com.xrosstools.idea.gef.EditorFacade;
import com.xrosstools.idea.gef.PanelContentProvider;
import com.xrosstools.idea.gef.actions.CommandExecutor;
import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.CommandStack;
import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.figures.Endpoint;
import com.xrosstools.idea.gef.figures.Figure;
import com.xrosstools.idea.gef.parts.*;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 图形编辑器交互控制器，完全独立于 AWT/Swing，
 * 使用自定义事件数据对象与前端交互。
 */
public class EditorInteraction<T extends IPropertySource> implements CommandExecutor, InteractionHandle {
    private AtomicReference<T> diagramRef = new AtomicReference<>();
    private List<ContentChangeListener<T>> listeners = new ArrayList<>();

    private PanelContentProvider<T> contentProvider;
    private ContextMenuProvider contextMenuBuilder;
    private ContextMenuProvider outlineContextMenuProvider;

    private AbstractGraphicalEditPart root;
    private AbstractTreeEditPart treeRoot;

    private EditorFacade<T> editorFacade;
    private Point lastHit;
    private Figure lastSelected;
    private Figure lastHover;
    private Point lastHoverLocation;
    private boolean isRightButton;

    private Object newModel;
    private AbstractGraphicalEditPart sourcePart;

    private CommandStack commandStack = new CommandStack();
    private AtomicBoolean inProcessing = new AtomicBoolean(false);

    // 当前状态处理器
    private InteractionHandle curHandle;

    // 预定义状态
    private final InteractionHandle readyHandle = new ReadyHandle();
    private final InteractionHandle figureSelectedHandle = new FigureSelectedHandle();
    private final InteractionHandle modelCreatedHandle = new ModelCreatedHandle();
    private final InteractionHandle connectionCreatedHandle = new ConnectionCreatedHandle();
    private final InteractionHandle sourceSelectedHandle = new SourceSelectedHandle();
    private final InteractionHandle sourceEndpointSelectedHandle = new SourceEndpointSelectedHandle();
    private final InteractionHandle targetEndpointSelectedHandle = new TargetEndpointSelectedHandle();
    private final InteractionHandle adjusterEndpointSelectedHandle = new AdjusterEndpointSelectedHandle();

    public EditorInteraction(EditorFacade editorFacade, PanelContentProvider<T> contentProvider) {
        this.editorFacade = editorFacade;
        this.contentProvider = contentProvider;

        contextMenuBuilder = contentProvider.getContextMenuProvider();
        outlineContextMenuProvider = contentProvider.getOutlineContextMenuProvider();

        contextMenuBuilder.setExecutor(this);
        outlineContextMenuProvider.setExecutor(this);
        curHandle = readyHandle;
    }

    public T getModel() {
        return diagramRef.get();
    }

    public void setModel(T model) {
        diagramRef.set(model);
        contentChanged(getModel());

        EditContext editContext = new EditContext();
        EditPartFactory editPartFactory = contentProvider.createEditPartFactory();
        EditPartFactory treeEditPartFactory = contentProvider.createTreePartFactory();

        AbstractGraphicalEditPart root = (AbstractGraphicalEditPart) editPartFactory.createEditPart(editContext, null, model);
        root.activate();

        AbstractTreeEditPart treeRoot = (AbstractTreeEditPart) treeEditPartFactory.createEditPart(editContext, null, model);
        treeRoot.activate();
        /**
         *         contentProvider.preBuildRoot();
         *
         *         root.refresh();
         *         treeRoot.refresh();
         *         contentProvider.postBuildRoot();
         */
    }

    public void register(ContentChangeListener listener) {
        listeners.add(listener);
    }

    private void contentChanged(T content) {
        for (ContentChangeListener<T> listener : listeners)
            listener.contentChanged(content);
    }

    public CommandStack getCommandStack() {
        return commandStack;
    }

    public AbstractGraphicalEditPart getRoot() {
        return root;
    }

    public AbstractTreeEditPart getTreeRoot() {
        return treeRoot;
    }

    public boolean isInProcessing() {
        return inProcessing.get();
    }

    // ----- 外部调用接口 -----

    public void reset() {
        inProcessing.set(false);
        gotoNext(readyHandle);
    }

    public void createConnection(Object connModel) {
        newModel = connModel;
        gotoNext(connectionCreatedHandle);
    }

    public void createModel(Object model) {
        newModel = model;
        gotoNext(modelCreatedHandle);
    }

    public void selectModel(Object selectedModel) {
        Figure selected = root.findFigure(selectedModel);
        if (selected != null) {
            selected.setSelected(true);
            updateFigureSelection(selected);
            editorFacade.updateSelectedModel(selectedModel);
            lastHit = null;
            gotoNext(figureSelectedHandle);
        } else {
            gotoNext(readyHandle);
        }
    }

    public void selectTreeModel(Object selectedModel) {
        if (inProcessing.get()) return;
        selectModel(selectedModel);
    }

    public JPopupMenu getTreePopupMenu(Object selected) {
        return outlineContextMenuProvider.buildDisplayMenu(selected);
    }

    // ----- 命令执行 -----

    @Override
    public void execute(Command command) {
        if (command == null || !command.canExecute()) return;

        Object model = (newModel != null) ? newModel : (lastSelected != null ? lastSelected.getPart().getModel() : null);
        inProcessing.set(true);
        commandStack.execute(command, model);

        if (newModel != null) newModel = null;
        postExecute(model);
    }

    public void undo() {
        inProcessing.set(true);
        commandStack.undo();
        postExecute(commandStack.getCurModel());
    }

    public void redo() {
        inProcessing.set(true);
        commandStack.redo();
        postExecute(commandStack.getCurModel());
    }

    private void postExecute(Object model) {
        refresh();
        editorFacade.save(getModel());

        AbstractGraphicalEditPart part = root.findEditPart(model);
        model = (part == null) ? getModel() : model;

        Figure selected = root.findFigure(model);
        if (selected == null || !selected.isSelectable()) {
            model = getModel();
        }
        selectModel(model);
        clearHover();
        inProcessing.set(false);
        refreshVisual();
    }

    private void refresh() {
        root.refresh();
        treeRoot.refresh();
    }

    // ----- 内部辅助方法 -----

    private void gotoNext(InteractionHandle next) {
        curHandle.leave();
        next.enter();
        curHandle = next;
    }

    private void updateFigureSelection(Figure selected) {
        if (lastSelected == selected) return;
        if (lastSelected != null) lastSelected.setSelected(false);
        lastSelected = selected;
        if (lastSelected != null) lastSelected.setSelected(true);
        editorFacade.updateSelectedFigure(lastSelected);
    }

    private void selectFigureAt(Point location) {
        Figure f = findFigureAt(location);
        updateFigureSelection(f);
        Object model = (f == null) ? null : f.getPart().getModel();
        editorFacade.updateSelectedModel(model);

        if (f == null) {
            gotoNext(readyHandle);
            return;
        }

        if (f instanceof Endpoint && f.getParent() instanceof Connection) {
            Endpoint endpoint = (Endpoint) f;
            if (endpoint.isConnectionSourceEndpoint())
                gotoNext(sourceEndpointSelectedHandle);
            else if (endpoint.isConnectionTargetEndpoint())
                gotoNext(targetEndpointSelectedHandle);
            else if (endpoint.isConnectionAdjusterEndpoint())
                gotoNext(adjusterEndpointSelectedHandle);
        }
    }

    private Figure findFigureAt(Point location) {
        Figure rootFigure = root.getFigure();
        Figure selected = rootFigure.selectFigureAt(location.x, location.y);
        return (selected == null) ? rootFigure : selected;
    }

    private void updateTooltip(Point location) {
        Figure f = findFigureAt(location);
        if (f == null || f == root.getFigure()) {
            editorFacade.setToolTipText(null);
        } else {
            editorFacade.setToolTipText(f.getToolTipText());
        }
    }

    private void updateHover(Figure underPoint, Point location, Command cmd, boolean showInsertionFeedback) {
        underPoint = (underPoint == null) ? root.getFigure() : underPoint;
        if (lastHover != null && lastHover != underPoint) {
            lastHover.getPart().getContentPane().setInsertionPoint(null);
        }
        if (cmd != null && showInsertionFeedback) {
            Point local = toLocalPoint(underPoint, location);
            underPoint.getPart().getContentPane().setInsertionPoint(local);
        }
        lastHoverLocation = location;
        lastHover = underPoint;
        repaint();
    }

    private void clearHover() {
        if (lastHover == null) return;
        lastHover.getPart().getContentPane().setInsertionPoint(null);
        editorFacade.updateRootFigure(root.getFigure());
        lastHover = null;
        lastHoverLocation = null;
        repaint();
    }

    private Point toLocalPoint(Figure target, Point location) {
        Point p = new Point(location);
        Figure contentPane = target.getPart().getContentPane();
        contentPane.translateToRelative(p);
        contentPane.translateFromParent(p);
        return p;
    }

    private void repaint() {
        editorFacade.updateRootFigure(root.getFigure());
        editorFacade.updateFeedbackFigure(curHandle.getFeedback());
        editorFacade.refreshVisual();
    }

    private void refreshVisual() {
        editorFacade.refreshVisual();
    }

    private void showContextMenu(int x, int y) {
        if (lastSelected == null) return;
        editorFacade.showContextMenu(x, y,
                contextMenuBuilder.buildDisplayMenu(lastSelected.getPart()));
    }

    // ----- InteractionHandle 默认实现 (主控状态) -----

    // 注意：所有鼠标/键盘方法均被转发给当前状态 curHandle
    @Override
    public void mousePressed(MouseEventData e) { curHandle.mousePressed(e); }
    @Override
    public void mouseReleased(MouseEventData e) { curHandle.mouseReleased(e); }
    @Override
    public void mouseClicked(MouseEventData e) { curHandle.mouseClicked(e); }
    @Override
    public void mouseMoved(MouseEventData e) { curHandle.mouseMoved(e); }
    @Override
    public void mouseDragged(MouseEventData e) { curHandle.mouseDragged(e); }
    @Override
    public void mouseEntered(MouseEventData e) { curHandle.mouseEntered(e); }
    @Override
    public void mouseExited(MouseEventData e) { curHandle.mouseExited(e); }
    @Override
    public void mouseWheelMoved(MouseEventData e) { curHandle.mouseWheelMoved(e); }
    @Override
    public void keyTyped(KeyEventData e) { curHandle.keyTyped(e); }
    @Override
    public void keyPressed(KeyEventData e) { curHandle.keyPressed(e); }
    @Override
    public void keyReleased(KeyEventData e) { curHandle.keyReleased(e); }

    // ----- 内部状态实现（与原始逻辑一致，但使用自定义事件） -----

    private class ReadyHandle implements InteractionHandle {
        @Override
        public void enter() {
            if (lastSelected != null) {
                lastSelected.setSelected(false);
                lastSelected = null;
            }
            newModel = null;
            sourcePart = null;
            clearHover();
            refreshVisual();
        }

        @Override
        public void mouseMoved(MouseEventData e) {
            updateTooltip(e.getPoint());
        }

        @Override
        public void mousePressed(MouseEventData e) {
            selectFigureAt(e.getPoint());
            lastHit = new Point(e.getPoint());
            isRightButton = (e.getButton() == MouseEventData.BUTTON3);
            gotoNext(figureSelectedHandle);
        }
    }

    private class FigureSelectedHandle implements InteractionHandle {
        private boolean moved;
        private Point delta;

        private boolean isApplicable() {
            return !(lastSelected.getPart().getModel() == getModel() || lastSelected instanceof Connection);
        }

        private boolean isAddCommand(Point p, Figure underPoint) {
            Point pos = new Point(p);
            lastSelected.translateToRelative(pos);
            return !lastSelected.containsPoint(pos) &&
                    underPoint.getPart() != lastSelected.getPart().getParent() &&
                    underPoint != lastSelected;
        }

        private Command getCommand(boolean isAdd, Figure target, Point p) {
            AbstractGraphicalEditPart part = lastSelected.getPart();
            AbstractGraphicalEditPart parentPart = target.getPart();
            EditPolicy policy = parentPart.getEditPolicy();
            if (policy == null) return null;

            Point localPoint = toLocalPoint(parentPart.getFigure(), p);
            Rectangle constrain = new Rectangle(
                    localPoint.x + delta.x,
                    localPoint.y + delta.y,
                    lastSelected.getWidth(),
                    lastSelected.getHeight()
            );
            return isAdd ? policy.getAddCommand(part, constrain) : policy.getMoveCommand(part, constrain);
        }

        private Figure getTarget(boolean isAdd, Figure underPoint) {
            return isAdd ? underPoint.getPart().getFigure() :
                    ((AbstractGraphicalEditPart) lastSelected.getPart().getParent()).getFigure();
        }

        private boolean showInsertionFeedback(Figure target, Command cmd) {
            return cmd != null && target.getPart().getEditPolicy().isInsertable(cmd);
        }

        @Override
        public void enter() {
            moved = false;
            Point pos = lastSelected.getLocation();
            lastSelected.translateToAbsolute(pos);
            delta = new Point();
            if (lastHit != null) {
                delta.x = pos.x - lastHit.x;
                delta.y = pos.y - lastHit.y;
            }
        }

        @Override
        public void mouseMoved(MouseEventData e) {
            updateTooltip(e.getPoint());
        }

        @Override
        public void mousePressed(MouseEventData e) {
            selectFigureAt(e.getPoint());
            lastHit = new Point(e.getPoint());
            isRightButton = (e.getButton() == MouseEventData.BUTTON3);
            enter();
        }

        @Override
        public void mouseDragged(MouseEventData e) {
            Point p = e.getPoint();
            if (isRightButton || !isApplicable()) return;

            Figure underPoint = findFigureAt(p);
            boolean isAdd = isAddCommand(p, underPoint);
            Figure target = getTarget(isAdd, underPoint);
            Command cmd = getCommand(isAdd, target, p);
            moved = (cmd != null);
            updateHover(target, p, cmd, showInsertionFeedback(target, cmd));
        }

        @Override
        public void mouseReleased(MouseEventData e) {
            Point p = e.getPoint();
            if (e.isPopupTrigger() || e.getButton() == MouseEventData.BUTTON3) {
                showContextMenu(e.getX(), e.getY());
            } else if (moved && lastHover != null && isApplicable()) {
                Figure underPoint = findFigureAt(p);
                boolean isAdd = isAddCommand(p, underPoint);
                Figure target = getTarget(isAdd, underPoint);
                Command cmd = getCommand(isAdd, target, p);
                clearHover();
                execute(cmd);
            }
            moved = false;
        }

        @Override
        public void mouseClicked(MouseEventData e) {
            if (e.getClickCount() == 2 && lastSelected != null) {
                lastSelected.getPart().performAction();
            }
        }

        @Override
        public void keyPressed(KeyEventData e) {
            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE && lastSelected != null) {
                EditPolicy policy = lastSelected.getPart().getEditPolicy();
                if (policy != null) {
                    Command deleteCmd = policy.getDeleteCommand();
                    if (deleteCmd != null) execute(deleteCmd);
                }
            }
        }

        @Override
        public Figure getFeedback() {
            if (moved && lastHoverLocation != null) {
                lastHoverLocation.translate(delta.x, delta.y);
                lastSelected.setMoveFeedbackLocation(lastHoverLocation);
            } else {
                lastSelected.setMoveFeedbackLocation(null);
            }
            return null;
        }
    }

    private class ModelCreatedHandle implements InteractionHandle {
        private Command getCreateCommand(Figure underPoint, Point p) {
            EditPolicy policy = underPoint.getPart().getEditPolicy();
            if (policy == null) return null;
            Point local = toLocalPoint(underPoint, p);
            return policy.getCreateCommand(newModel, local);
        }

        private boolean showInsertionFeedback(Figure underPoint, Command cmd) {
            return underPoint.getPart().getEditPolicy() != null &&
                    underPoint.getPart().getEditPolicy().isInsertable(cmd);
        }

        @Override
        public void mouseMoved(MouseEventData e) {
            Point p = e.getPoint();
            Figure underPoint = findFigureAt(p);
            Command cmd = getCreateCommand(underPoint, p);
            updateHover(underPoint, p, cmd, showInsertionFeedback(underPoint, cmd));
        }

        @Override
        public void mousePressed(MouseEventData e) {
            Point p = e.getPoint();
            Figure underPoint = findFigureAt(p);
            Command cmd = getCreateCommand(underPoint, p);
            execute(cmd);
            gotoNext(readyHandle);
        }

        @Override
        public void keyPressed(KeyEventData e) {
            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                gotoNext(readyHandle);
            }
        }
    }

    private class ConnectionCreatedHandle implements InteractionHandle {
        @Override
        public void mouseMoved(MouseEventData e) {
            Figure f = findFigureAt(e.getPoint());
            showSourceFeedback(f, isSelectableSource(f));
        }

        @Override
        public void mousePressed(MouseEventData e) {
            eraseSourceFeedback();
            Figure f = findFigureAt(e.getPoint());
            if (isSelectableSource(f)) {
                sourcePart = f.getPart();
                gotoNext(sourceSelectedHandle);
            } else {
                gotoNext(readyHandle);
            }
        }

        @Override
        public void keyPressed(KeyEventData e) {
            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                eraseSourceFeedback();
                gotoNext(readyHandle);
            }
        }

        private boolean isSelectableSource(Figure f) {
            return f != null && f.getPart() != null && f.getPart().getEditPolicy() != null &&
                    f.getPart().getEditPolicy().isSelectableSource(newModel);
        }
    }

    private Figure lastSelectableSource;
    private void showSourceFeedback(Figure f, boolean selectable) {
        if (f == null || f == lastSelectableSource) { repaint(); return; }
        eraseSourceFeedback();
        if (selectable) {
            f.getPart().showSourceFeedback();
            lastSelectableSource = f;
        }
        repaint();
    }
    private void eraseSourceFeedback() {
        if (lastSelectableSource != null) {
            lastSelectableSource.getPart().eraseSourceFeedback();
            lastSelectableSource = null;
        }
        repaint();
    }

    private class SourceSelectedHandle implements InteractionHandle {
        private Connection conn;

        @Override
        public void enter() {
            conn = new Connection();
            conn.setSourcePart(sourcePart);
            conn.relocateTargetFeedback(sourcePart.getFigure());
        }

        @Override
        public void mouseMoved(MouseEventData e) {
            lastHoverLocation = e.getPoint();
            Figure f = findFigureAt(lastHoverLocation);
            boolean targetSelectable = getCommand(f) != null;
            conn.relocateTargetFeedback(targetSelectable ? f : new Point(lastHoverLocation));
            showTargetFeedback(f, targetSelectable);
        }

        @Override
        public void mousePressed(MouseEventData e) {
            Figure f = findFigureAt(e.getPoint());
            eraseTargetFeedback();
            execute(getCommand(f));
            gotoNext(readyHandle);
        }

        @Override
        public void keyPressed(KeyEventData e) {
            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                eraseTargetFeedback();
                gotoNext(readyHandle);
            }
        }

        @Override
        public Figure getFeedback() { return conn; }

        private Command getCommand(Figure underPoint) {
            if (underPoint == null) return null;
            EditPolicy policy = underPoint.getPart().getEditPolicy();
            if (policy == null) return null;
            return policy.getCreateConnectionCommand(newModel, sourcePart);
        }
    }

    private Figure lastSelectableTarget;
    private void showTargetFeedback(Figure f, boolean selectable) {
        if (f == null || f == lastSelectableTarget) { repaint(); return; }
        eraseTargetFeedback();
        if (selectable) {
            f.getPart().showTargetFeedback();
            lastSelectableTarget = f;
        }
        repaint();
    }
    private void eraseTargetFeedback() {
        if (lastSelectableTarget != null) {
            lastSelectableTarget.getPart().eraseTargetFeedback();
            lastSelectableTarget = null;
        }
        repaint();
    }

    private class SourceEndpointSelectedHandle implements InteractionHandle {
        private Endpoint endpoint;

        @Override
        public void enter() {
            endpoint = (Endpoint) lastSelected;
        }

        @Override
        public void mousePressed(MouseEventData e) {
            selectFigureAt(e.getPoint());
            if (lastSelected == endpoint) return;
            endpoint = null;
            gotoNext(figureSelectedHandle);
        }

        @Override
        public void mouseDragged(MouseEventData e) {
            lastHoverLocation = e.getPoint();
            Figure f = findFigureAt(e.getPoint());
            boolean selectable = getCommand(f) != null;
            endpoint.getParentConnection().relocateSourceFeedback(selectable ? f : lastHoverLocation);
            showSourceFeedback(f, selectable);
        }

        @Override
        public void mouseReleased(MouseEventData e) {
            Figure f = findFigureAt(e.getPoint());
            endpoint.getParentConnection().clearFeedback();
            eraseSourceFeedback();
            execute(getCommand(f));
            gotoNext(readyHandle);
        }

        private Command getCommand(Figure underPoint) {
            if (underPoint == null) return null;
            EditPolicy policy = underPoint.getPart().getEditPolicy();
            if (policy == null) return null;
            Connection connection = ((Endpoint) lastSelected).getParentConnection();
            return policy.getReconnectSourceCommand(connection.getConnectionPart());
        }
    }

    private class TargetEndpointSelectedHandle implements InteractionHandle {
        private Endpoint endpoint;

        @Override
        public void enter() {
            endpoint = (Endpoint) lastSelected;
        }

        @Override
        public void mousePressed(MouseEventData e) {
            selectFigureAt(e.getPoint());
            if (lastSelected == endpoint) return;
            endpoint = null;
            gotoNext(figureSelectedHandle);
        }

        @Override
        public void mouseDragged(MouseEventData e) {
            lastHoverLocation = e.getPoint();
            Figure f = findFigureAt(e.getPoint());
            boolean selectable = getCommand(f) != null;
            endpoint.getParentConnection().relocateTargetFeedback(selectable ? f : new Point(lastHoverLocation));
            showTargetFeedback(f, selectable);
        }

        @Override
        public void mouseReleased(MouseEventData e) {
            Figure f = findFigureAt(e.getPoint());
            endpoint.getParentConnection().clearFeedback();
            eraseTargetFeedback();
            execute(getCommand(f));
            gotoNext(readyHandle);
        }

        private Command getCommand(Figure underPoint) {
            if (underPoint == null) return null;
            EditPolicy policy = underPoint.getPart().getEditPolicy();
            if (policy == null) return null;
            Connection connection = ((Endpoint) lastSelected).getParentConnection();
            return policy.getReconnectTargetCommand(connection.getConnectionPart());
        }
    }

    private class AdjusterEndpointSelectedHandle implements InteractionHandle {
        private Endpoint endpoint;

        @Override
        public void enter() {
            endpoint = (Endpoint) lastSelected;
        }

        @Override
        public void mousePressed(MouseEventData e) {
            selectFigureAt(e.getPoint());
            if (lastSelected == endpoint) return;
            endpoint = null;
            gotoNext(figureSelectedHandle);
        }

        @Override
        public void mouseDragged(MouseEventData e) {
            endpoint.setAdjustment(e.getPoint());
            repaint();
        }

        @Override
        public void mouseReleased(MouseEventData e) {
            endpoint.setAdjustment(e.getPoint());
            execute(getCommand());
            gotoNext(readyHandle);
        }

        private Command getCommand() {
            EditPolicy policy = endpoint.getPart().getEditPolicy();
            if (policy == null) return null;
            Connection connection = ((Endpoint) lastSelected).getParentConnection();
            return policy.getAdjustConnectionCommand(connection.getConnectionPart());
        }
    }
}