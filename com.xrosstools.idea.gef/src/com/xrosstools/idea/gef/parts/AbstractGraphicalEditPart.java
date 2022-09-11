package com.xrosstools.idea.gef.parts;

import com.xrosstools.idea.gef.figures.AbstractAnchor;
import com.xrosstools.idea.gef.figures.ChopboxAnchor;
import com.xrosstools.idea.gef.figures.Figure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractGraphicalEditPart extends AbstractEditPart {
    public static final int SELECTED_NONE = 0;

    public static final int SELECTED = 1;

    private Figure figure;
    private int flags;
    private List<AbstractGraphicalEditPart> childEditParts = new ArrayList<>();
    private List<AbstractConnectionEditPart> sourceConnEditParts = new ArrayList<>();
    private List<AbstractConnectionEditPart> targteConnEditParts = new ArrayList<>();
    private int selected;

    private EditPolicy editPolicy;

    protected abstract Figure createFigure();
    protected EditPolicy createEditPolicy() {
        return new EditPolicy();
    }

    /**
     * Being double clicked
     */
    public void performAction() {}

    public List<AbstractGraphicalEditPart> getChildren() {
        return childEditParts;
    }

    public List<AbstractConnectionEditPart> getSourceConnections() {
        return sourceConnEditParts;
    }

    public List getModelSourceConnections() {
        return Collections.EMPTY_LIST;
    }

    public List<AbstractConnectionEditPart> getTargetConnections() {
        return targteConnEditParts;
    }

    public List getModelTargetConnections() {
        return Collections.EMPTY_LIST;
    }

    public void addConnectionVisual(AbstractConnectionEditPart childEditPart, int index) {
        getFigure().add(childEditPart.getFigure(), index);
    }

    public void addChildVisual(EditPart childEditPart, int index) {
        getFigure().add(((AbstractGraphicalEditPart)childEditPart).getFigure(), index);
    }

    protected void addChildPartVisual(EditPart childEditPart, int index) {
        if(childEditPart instanceof AbstractConnectionEditPart) {
            addConnectionVisual((AbstractConnectionEditPart)childEditPart, index);
        }else {
            addChildVisual(childEditPart, index);
        }
    }

    public void addNotify(){
        getFigure();
        super.addNotify();
    }

    protected void removeChildVisual(EditPart childEditPart) {
        getFigure().remove(((AbstractGraphicalEditPart)childEditPart).getFigure());
    }

    public final Figure getFigure() {
        if (figure == null) {
            figure = createFigure();
            figure.setPart(this);
            figure.setRootPane(getContext().getContentPane());
        }
        return figure;
    }

    public  Figure getContentPane() {
        return getFigure();
    }

    public final EditPolicy getEditPolicy() {
        if (editPolicy == null) {
            editPolicy = createEditPolicy();
            editPolicy.setHost(this);
        }
        return editPolicy;
    }

    public void setLayoutConstraint(EditPart editpart, Figure figure, Object obj) {
        getContentPane().getLayoutManager().setConstraint(figure, obj);
    }

    public boolean isSelectable() {
        return getFigure() != null;
    }

    public final AbstractGraphicalEditPart findEditPart(Object model) {
        return getContext().findEditPart(model);
    }

    public final int getSelected() {
        return selected;
    }

    public void showSourceFeedback() {
        getFigure().setShowSourceFeedback(true);
    }

    public void eraseSourceFeedback() {
        getFigure().setShowSourceFeedback(false);
    }

    public void showTargetFeedback() {
        getFigure().setShowTargetFeedback(true);
    }

    public void eraseTargetFeedback() {
        getFigure().setShowTargetFeedback(false);
    }

    public AbstractAnchor getSourceConnectionAnchor(AbstractConnectionEditPart connectionEditPart) {
        return new ChopboxAnchor(getFigure());
    }

    public AbstractAnchor getTargetConnectionAnchor(AbstractConnectionEditPart connectionEditPart) {
        return new ChopboxAnchor(getFigure());
    }

    public void refresh() {
        super.refresh();
        refreshSourceConnections();
        refreshTargetConnections();
    }

    protected void refreshSourceConnections() {
        refreshModelPart(getSourceConnections(), getModelSourceConnections(), sourceConnectionHandler);
    }

    protected void refreshTargetConnections() {
        refreshModelPart(getTargetConnections(), getModelTargetConnections(), targetConnectionHandler);
    }

    private EditPartHandler sourceConnectionHandler = new EditPartHandler() {
        @Override
        public void reorderChild(List parts, EditPart childPart, int index) {
            defaultReorder(parts, childPart, index);
        }

        @Override
        public void addChildModel(List parts, Object child, int index) {
            AbstractConnectionEditPart connection = (AbstractConnectionEditPart)createOrFindPart(child);
            parts.add(index, connection);

            AbstractGraphicalEditPart source = connection.getSource();
            if (source != null)
                source.getSourceConnections().remove(connection);

            connection.setSource(AbstractGraphicalEditPart.this);
            addChildPartVisual(connection, index);

            connection.addNotify();
            connection.activate();
        }

        @Override
        public void removeChild(List parts, EditPart childEditPart) {
            AbstractConnectionEditPart connection = (AbstractConnectionEditPart)childEditPart;
            if (connection.getSource() == AbstractGraphicalEditPart.this) {
                connection.deactivate();
                removeChildVisual(childEditPart);
                connection.setSource(null);
            }
            parts.remove(childEditPart);
        }
    };

    private EditPartHandler targetConnectionHandler = new EditPartHandler() {
        @Override
        public void reorderChild(List parts, EditPart editPart, int index) {
            defaultReorder(parts, editPart, index);
        }

        @Override
        public void addChildModel(List parts, Object child, int index) {
            AbstractConnectionEditPart connection = (AbstractConnectionEditPart)createOrFindPart(child);
            parts.add(index, connection);

            AbstractGraphicalEditPart target = connection.getTarget();
            if (target != null)
                target.getTargetConnections().remove(connection);
            connection.setTarget(AbstractGraphicalEditPart.this);
        }

        @Override
        public void removeChild(List parts, EditPart childEditPart) {
            AbstractConnectionEditPart connection = (AbstractConnectionEditPart)childEditPart;
            if (connection.getTarget() == AbstractGraphicalEditPart.this)
                connection.setTarget(null);
            parts.remove(childEditPart);
        }
    };
}
