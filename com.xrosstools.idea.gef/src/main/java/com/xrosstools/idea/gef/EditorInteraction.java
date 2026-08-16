package com.xrosstools.idea.gef;

import com.xrosstools.idea.gef.actions.CommandExecutor;
import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.CommandStack;
import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.figures.Endpoint;
import com.xrosstools.idea.gef.figures.Figure;
import com.xrosstools.idea.gef.parts.AbstractGraphicalEditPart;
import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;
import com.xrosstools.idea.gef.parts.EditPolicy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class EditorInteraction<T> implements CommandExecutor {
    private AtomicReference<T> diagramRef = new AtomicReference<>();
    private ContextMenuProvider contextMenuBuilder;

    private AbstractGraphicalEditPart root;
    private AbstractTreeEditPart treeRoot;

    private EditorFacade editorFacade;
    private Point lastHit;
    private Figure lastSelected;
    private Figure lastHover;
    private Point lastHoverLocation;
    private boolean isRightButton;

    private Object newModel;
    private AbstractGraphicalEditPart sourcePart;

    private CommandStack commandStack = new CommandStack();
    private AtomicBoolean inProcessing = new AtomicBoolean(false);

    private AtomicBoolean saving = new AtomicBoolean(false);

    public EditorInteraction(EditorFacade editorFacade, ContextMenuProvider contextMenuBuilder) {
        this.editorFacade = editorFacade;
        this.contextMenuBuilder = contextMenuBuilder;
        contextMenuBuilder.setExecutor(this);

    }

    public T getModel() {
        return diagramRef.get();
    }

    public void setModel(T model) {
        diagramRef.set(model);
    }

    public CommandStack getCommandStack() {
        return commandStack;
    }

    public void setRoots(AbstractGraphicalEditPart root, AbstractTreeEditPart treeRoot){
        this.root = root;
        this.treeRoot = treeRoot;
    }

    public void registerListener(JPanel unitPanel) {
        unitPanel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                curHandle.mouseDragged(e);
            }

            public void mouseMoved(MouseEvent e) {
                curHandle.mouseMoved(e);
            }
        });

        unitPanel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                curHandle.mouseClicked(e);
            }

            public void mousePressed(MouseEvent e) {
                curHandle.mousePressed(e);
            }

            public void mouseReleased(MouseEvent e) {
                curHandle.mouseReleased(e);
            }

            public void mouseEntered(MouseEvent e) {
                curHandle.mouseEntered(e);
            }

            public void mouseExited(MouseEvent e) {
                curHandle.mouseExited(e);
            }

            public void mouseWheelMoved(MouseWheelEvent e) {
                curHandle.mouseWheelMoved(e);
            }
        });

        unitPanel.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                curHandle.keyTyped(e);
            }

            public void keyPressed(KeyEvent e) {
                curHandle.keyPressed(e);
            }

            public void keyReleased(KeyEvent e) {
                curHandle.keyReleased(e);
            }
        });
    }

    public void reset() {
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

    public void selectModel(Object selectedModel) {
        Figure selected = root.findFigure(selectedModel);
        selected.setSelected(true);
        updateFigureSelection(selected);

        editorFacade.updateSelectedModel(selectedModel);

        if (selected == null) {
            gotoNext(ready);
        } else {
            lastHit = null;
            gotoNext(figureSelected);
        }
    }

    public void selectTreeModel(Object selectedModel) {
        if (inProcessing.get())
            return;

        Figure selected = root.findFigure(selectedModel);
        updateFigureSelection(selected);

        editorFacade.updateSelectedModel(selectedModel);

        if (selected == null) {
            gotoNext(ready);
        } else {
            lastHit = null;
            gotoNext(figureSelected);
        }
    }

    private void updateFigureSelection(Figure selected) {
        if (lastSelected == selected)
            return;

        if (lastSelected != null)
            lastSelected.setSelected(false);

        lastSelected = selected;
        if (lastSelected != null)
            lastSelected.setSelected(true);

        editorFacade.updateSelectedFigure(lastSelected);
    }

    private void selectFigureAt(Point location) {
        Figure f = findFigureAt(location);
        updateFigureSelection(f);

        Object model = f == null ? null : f.getPart().getModel();
        editorFacade.updateSelectedModel(model);

        if (f == null) {
            gotoNext(ready);
            return;
        }

        if (f instanceof Endpoint && f.getParent() instanceof Connection) {
            Endpoint endpoint = (Endpoint) f;
            if (endpoint.isConnectionSourceEndpoint())
                gotoNext(sourceEndpointSelected);
            else if (endpoint.isConnectionTargetEndpoint())
                gotoNext(targetEndpointSelected);
            else if (endpoint.isConnectionAdjusterEndpoint())
                gotoNext(adjusterEndpointSelected);
        }
    }

    private void updateTooltip(Point location) {
        Figure f = findFigureAt(location);
        if (f == null || f == root.getFigure())
            editorFacade.setToolTipText(null);
        else {
            editorFacade.setToolTipText(f.getToolTipText());
        }
    }

    private Figure findFigureAt(Point location) {
        Figure rootFigure = root.getFigure();
        Figure selected = rootFigure.selectFigureAt(location.x, location.y);
        return selected == null ? rootFigure : selected;
    }

    private void updateHover(Figure underPoint, Point location, Command cmd, boolean showInsertionFeedback) {
        underPoint = underPoint == null ? root.getFigure() : underPoint;

        if (lastHover != null && lastHover != underPoint) {
            lastHover.getPart().getContentPane().setInsertionPoint(null);
        }

        if (cmd != null && showInsertionFeedback) {
            Point localLocation = toLocalPoint(underPoint, location);
            underPoint.getPart().getContentPane().setInsertionPoint(localLocation);
        }

        lastHoverLocation = location;
        lastHover = underPoint;
        repaint();
    }

    private void clearHover() {
        if (lastHover == null)
            return;

        lastHover.getPart().getContentPane().setInsertionPoint(null);
        editorFacade.updateRootFigure(root.getFigure());
        lastHover = null;
        lastHoverLocation = null;
        repaint();
    }

    private void refreshVisual() {
        editorFacade.refreshVisual();
    }

    private void showContextMenu(int x, int y) {
        editorFacade.showContextMenu(x, y, contextMenuBuilder.buildDisplayMenu(lastSelected.getPart()));
    }

    public void execute(Command command) {
        if (command == null || command.canExecute() == false)
            return;

        Object model = newModel;
        if (model == null)
            model = lastSelected == null ? null : lastSelected.getPart().getModel();
        inProcessing.set(true);
        commandStack.execute(command, model);

        if (newModel != null)
            newModel = null;

        postExecute(model);
    }

    private void refresh() {
        root.refresh();
        treeRoot.refresh();
    }

    private void postExecute(Object model) {
        refresh();
        editorFacade.save(getModel());

        AbstractGraphicalEditPart part = root.findEditPart(model);
        model = part == null ? getModel() : model;

        Figure selected = root.findFigure(model);
        if (selected == null || !selected.isSelectable())
            model = getModel();

        selectModel(model);
        clearHover();
        inProcessing.set(false);
        refreshVisual();
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

    public void gotoNext(InteractionHandle next) {
        curHandle.leave();
        next.enter();
        curHandle = next;
    }

    private boolean isPopupTrigger(MouseEvent evt) {
        return evt.isPopupTrigger() || evt.getButton() == MouseEvent.BUTTON3;
    }

    private class InteractionHandle extends MouseAdapter implements KeyListener {
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
        }

        public void enter() {
        }

        public void leave() {
        }

        //Only return those feedback that can not be found in root figure, like newly created connection, etc
        public Figure getFeedback() {
            return null;
        }

        public String id;

        public InteractionHandle(String id) {
            this.id = id;
        }
    }

    private InteractionHandle ready = new InteractionHandle("ready") {
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

    private void repaint() {
        editorFacade.updateRootFigure(root.getFigure());
        editorFacade.updateFeedbackFigure(curHandle.getFeedback());
        editorFacade.refreshVisual();
    }

    private InteractionHandle figureSelected = new InteractionHandle("figureSelected") {
        private boolean moved;
        private Point delta;

        private boolean isApplicable() {
            if (lastSelected.getPart().getModel() == getModel())
                return false;

            if (lastSelected instanceof Connection)
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
            if (policy == null)
                return null;

            Rectangle constrain = new Rectangle(localPoint.x + delta.x, localPoint.y + delta.y, lastSelected.getWidth(), lastSelected.getHeight());
            return isAdd ? policy.getAddCommand(part, constrain) : policy.getMoveCommand(part, constrain);
        }

        private Figure getTarget(boolean isAdd, Figure underPoint) {
            return isAdd ? underPoint.getPart().getFigure() : ((AbstractGraphicalEditPart) lastSelected.getPart().getParent()).getFigure();
        }

        private boolean showInsertionFeedback(Figure target, Command cmd) {
            return cmd != null && target.getPart().getEditPolicy().isInsertable(cmd);
        }

        public void enter() {
            moved = false;
            Point pos = lastSelected.getLocation();
            lastSelected.translateToAbsolute(pos);
            delta = new Point();

            if (lastHit == null)
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
            if (isRightButton || !isApplicable())
                return;

            Figure underPoint = findFigureAt(p);
            boolean isAdd = isAddCommand(p, underPoint);
            Figure target = getTarget(isAdd, underPoint);
            Command cmd = getCommand(isAdd, target, p);
            moved = cmd != null;

            updateHover(target, p, cmd, showInsertionFeedback(target, cmd));
        }

        public void mouseReleased(MouseEvent e) {
            if (isPopupTrigger(e))
                showContextMenu(e.getX(), e.getY());
            else if (moved && lastHover != null && isApplicable()) {
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
            if (e.getClickCount() == 2) {
                lastSelected.getPart().performAction();
            }
        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                EditPolicy policy = lastSelected.getPart().getEditPolicy();
                if (policy == null)
                    return;

                Command deleteCmd = policy.getDeleteCommand();
                if (deleteCmd == null)
                    return;

                execute(deleteCmd);
            }
        }

        public Figure getFeedback() {
            if (moved) {
                lastHoverLocation.translate(delta.x, delta.y);
                lastSelected.setMoveFeedbackLocation(lastHoverLocation);
            } else {
                lastSelected.setMoveFeedbackLocation(null);
            }
            return null;
        }
    };

    private InteractionHandle modelCreated = new InteractionHandle("modelCreated") {
        private Command getCreateCommand(Figure underPoint, Point p) {
            EditPolicy policy = underPoint.getPart().getEditPolicy();
            if (policy == null) return null;

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
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
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
            if (isSelectableSource(f)) {
                sourcePart = f.getPart();
                gotoNext(sourceSelected);
            } else {
                gotoNext(ready);
            }
        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
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
        if (f == null || f == lastSelectableSource) {
            repaint();
            return;
        }

        eraseSourceFeedback();
        if (isSelectableSource) {
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
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                eraseTargetFeedback();
                gotoNext(ready);
            }
        }

        public Figure getFeedback() {
            return conn;
        }

        private Command getCommand(Figure underPoint) {
            EditPolicy policy = underPoint.getPart().getEditPolicy();
            if (policy == null) return null;

            return policy.getCreateConnectionCommand(newModel, sourcePart);
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
        if (lastSelectableTarget != null) {
            lastSelectableTarget.getPart().eraseTargetFeedback();
            lastSelectableTarget = null;
            repaint();
        }
    }

    private InteractionHandle sourceEndpointSelected = new InteractionHandle("sourceEndpointSelected") {
        private Endpoint endpoint;

        public void enter() {
            endpoint = (Endpoint) lastSelected;
        }

        public void mousePressed(MouseEvent e) {
            selectFigureAt(e.getPoint());
            if (lastSelected == endpoint)
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
            if (policy == null) return null;

            Connection connection = ((Endpoint) lastSelected).getParentConnection();
            return policy.getReconnectSourceCommand(connection.getConnectionPart());
        }
    };

    private InteractionHandle targetEndpointSelected = new InteractionHandle("targetEndpointSelected") {
        private Endpoint endpoint;

        public void enter() {
            endpoint = (Endpoint) lastSelected;
        }

        public void mousePressed(MouseEvent e) {
            selectFigureAt(e.getPoint());
            if (lastSelected == endpoint)
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
            if (policy == null) return null;

            Connection connection = ((Endpoint) lastSelected).getParentConnection();
            return policy.getReconnectTargetCommand(connection.getConnectionPart());
        }
    };

    private InteractionHandle adjusterEndpointSelected = new InteractionHandle("adjusterEndpointSelected") {
        private Endpoint endpoint;

        public void enter() {
            endpoint = (Endpoint) lastSelected;
        }

        public void mousePressed(MouseEvent e) {
            selectFigureAt(e.getPoint());
            if (lastSelected == endpoint)
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
            if (policy == null) return null;

            Connection connection = ((Endpoint) lastSelected).getParentConnection();
            return policy.getAdjustConnectionCommand(connection.getConnectionPart());
        }
    };

    private InteractionHandle curHandle = ready;
}
