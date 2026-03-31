package com.xrosstools.idea.gef.policies;

import com.xrosstools.idea.gef.commands.AddNodeCommand;
import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.CreateNodeCommand;
import com.xrosstools.idea.gef.commands.MoveNodeCommand;
import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeContainer;
import com.xrosstools.idea.gef.parts.AbstractGraphicalEditPart;
import com.xrosstools.idea.gef.parts.EditPolicy;

import java.awt.*;

public class NodeContainerEditPolicy extends EditPolicy {
    @Override
    public Command getCreateCommand(Object newModel, Point location) {
        if(!isCreateAllowed(newModel, location))
            return null;

        return createCreateNodeCommand().init(
                (NodeContainer) getHost().getModel(),
                (Node) newModel,
                location);
    }

    @Override
    public Command getMoveCommand(AbstractGraphicalEditPart child, Rectangle constraint) {
        if(!isMoveAllowed(child.getModel(), constraint))
            return null;

        return createMoveNodeCommand().init((Node) child.getModel(), constraint);
    }

    @Override
    public Command getAddCommand(AbstractGraphicalEditPart child, Rectangle constraint) {
        if(!(child.getParent().getModel() instanceof NodeContainer))
            return null;

        if(!isAddAllowed(child.getModel(), constraint))
            return null;

        return createAddNodeCommand().init(
                (NodeContainer)child.getParent().getModel(),
                (NodeContainer) getHost().getModel(),
                (Node) child.getModel(),
                constraint);
    }

    @Override
    public boolean isInsertable(Command cmd) {
        return true;
    }

    public MoveNodeCommand createMoveNodeCommand() {
        return new MoveNodeCommand();
    }

    public CreateNodeCommand createCreateNodeCommand() {
        return new CreateNodeCommand();
    }

    public AddNodeCommand createAddNodeCommand() {
        return new AddNodeCommand();
    }

    public boolean isCreateAllowed(Object model, Point location) {
        return isChildTypeAcceptable(model) && isLocationAcceptable(location);
    }

    public boolean isAddAllowed(Object model, Rectangle constraint) {
        if(!isChildTypeAcceptable(model))
            return false;

        Node node = (Node)model;
        //Do not allow move when there are connections on model
        if(!(node.getInputs().isEmpty() && node.getOutputs().isEmpty()))
            return false;

        return isChildTypeAcceptable(model) && isConstraintAcceptable(constraint);
    }

    public boolean isMoveAllowed(Object model, Rectangle constraint) {
        return isChildTypeAcceptable(model) && isConstraintAcceptable(constraint);
    }

    public boolean isChildTypeAcceptable(Object model) {
        return model instanceof Node;
    }

    public boolean isLocationAcceptable(Point location) {
        return true;
    }

    public boolean isConstraintAcceptable(Rectangle constraint) {
        return true;
    }
}