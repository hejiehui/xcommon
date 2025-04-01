package com.xrosstools.idea.gef.policies;

import com.xrosstools.idea.gef.commands.*;
import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeConnection;
import com.xrosstools.idea.gef.model.NodeContainer;
import com.xrosstools.idea.gef.parts.AbstractConnectionEditPart;
import com.xrosstools.idea.gef.parts.AbstractGraphicalEditPart;
import com.xrosstools.idea.gef.parts.EditPolicy;

import java.awt.*;

public class NodeEditPolicy extends EditPolicy {
    @Override
    public Command getDeleteCommand() {
        return createDeleteNodeCommand().init(
                (NodeContainer)(getHost().getParent().getModel()),
                (Node)(getHost().getModel()));
    }

    @Override
    public boolean isSelectableSource(Object connectionModel) {return true;}

    @Override
    public Command getChangeCommand(Rectangle constraint) {
        return createChangeCommand().init((Node) getHost().getModel(), constraint);
    }

    @Override
    public Command getCreateConnectionCommand(Object connectionModel, AbstractGraphicalEditPart sourcePart) {
        return createCreateConnectionCommand().init((NodeConnection) connectionModel, (Node) sourcePart.getModel(), (Node) getHost().getModel());
    }

    @Override
    public Command getReconnectSourceCommand(AbstractConnectionEditPart connectionPart) {
        return createReconnectSourceCommand().init(
                (NodeConnection) connectionPart.getModel(),
                (Node) getHost().getModel());
    }

    @Override
    public Command getReconnectTargetCommand(AbstractConnectionEditPart connectionPart) {
        return createReconnectTargetCommand().init(
                (NodeConnection) connectionPart.getModel(),
                (Node) getHost().getModel());
    }

    public DeleteNodeCommand createDeleteNodeCommand() {
        return new DeleteNodeCommand();
    }

    public MoveNodeCommand createChangeCommand() {
        return new MoveNodeCommand();
    }

    public CreateConnectionCommand createCreateConnectionCommand() {
        return new CreateConnectionCommand();
    }

    public ReconnectSourceCommand createReconnectSourceCommand() {
        return new ReconnectSourceCommand();
    }

    public ReconnectTargetCommand createReconnectTargetCommand() {
        return new ReconnectTargetCommand();
    }
}
