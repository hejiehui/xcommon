package com.xrosstools.idea.gef.policies;

import com.xrosstools.idea.gef.commands.AddOrderedNodeCommand;
import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.CreateOrderedNodeCommand;
import com.xrosstools.idea.gef.commands.MoveOrderedNodeCommand;
import com.xrosstools.idea.gef.model.NodeContainer;
import com.xrosstools.idea.gef.parts.AbstractGraphicalEditPart;
import com.xrosstools.idea.gef.parts.EditPolicy;

import java.awt.*;

public class OrderedContainerEditPolicy extends EditPolicy {
    @Override
    public Command getCreateCommand(Object newModel, Point location) {
        int index = getIndex(location);
        Object parent = getHost().getModel();
        if(!isCreateAllowed(index, newModel))
            return null;

        return createCreateNodeCommand().init(
                (NodeContainer) parent,
                newModel,
                index);
    }

    @Override
    public Command getMoveCommand(AbstractGraphicalEditPart child, Rectangle constraint) {
        Object model = child.getModel();
        Object parent = getHost().getModel();
        int index = getIndex(constraint.getLocation());
        if(!isMoveAllowed(index, model))
            return null;

        return createMoveNodeCommand().init(
                (NodeContainer)parent,
                model,
                index);
    }

    @Override
    public Command getAddCommand(AbstractGraphicalEditPart child, Rectangle constraint) {
        Object model = child.getModel();
        Object oldParent = child.getParent().getModel();
        Object newParent = getHost().getModel();

        if(!(oldParent instanceof NodeContainer && newParent instanceof NodeContainer))
            return null;

        int index = getIndex(constraint.getLocation());
        if(!isAddAllowed(index, model))
            return null;

        return createAdddNodeCommand().init(
                (NodeContainer)oldParent,
                (NodeContainer)newParent,
                model,
                index);
    }

    @Override
    public boolean isInsertable(Command cmd) {
        return true;
    }

    public MoveOrderedNodeCommand createMoveNodeCommand() {
        return new MoveOrderedNodeCommand();
    }

    public AddOrderedNodeCommand createAdddNodeCommand() {
        return new AddOrderedNodeCommand();
    }

    public CreateOrderedNodeCommand createCreateNodeCommand() {
        return new CreateOrderedNodeCommand();
    }

    public int getIndex(Point location) {
        return getHost().getFigure().getInsertionIndex(location);
    }

    public boolean isCreateAllowed(int index, Object model) {
        return isChildTypeAcceptable(model) && isIndexAcceptable(index);
    }

    public boolean isAddAllowed(int index, Object model) {
        return isChildTypeAcceptable(model) && isIndexAcceptable(index);
    }

    public boolean isMoveAllowed(int index, Object model) {
        return isChildTypeAcceptable(model) && isIndexAcceptable(index);
    }

    public boolean isChildTypeAcceptable(Object model) {
        return true;
    }

    public boolean isIndexAcceptable(int index) {
        return index >= 0;
    }
}