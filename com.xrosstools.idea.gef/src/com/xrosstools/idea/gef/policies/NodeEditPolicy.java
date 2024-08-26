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
    public Command getDeleteCommand() {
        return new DeleteNodeCommand(
                (NodeContainer)(getHost().getParent().getModel()),
                (Node)(getHost().getModel()));
    }

    public boolean isSelectableSource(Object connectionModel) {return true;}

    public Command getChangeCommand(Rectangle constraint) {
        MoveNodeCommand cmd = new MoveNodeCommand();
        cmd.setNode((Node) getHost().getModel());
        cmd.setConstraint(constraint);
        return cmd;
    }

    public Command getCreateConnectionCommand(Object connectionModel, AbstractGraphicalEditPart sourcePart) {
        return new CreateConnectionCommand((NodeConnection) connectionModel, (Node) sourcePart.getModel(), (Node) getHost().getModel());
    }

    public Command getReconnectSourceCommand(AbstractConnectionEditPart connectionPart) {
        return new ReconnectSourceCommand(
                (NodeConnection)connectionPart.getModel(),
                (Node)getHost().getModel());
    }

    public Command getReconnectTargetCommand(AbstractConnectionEditPart connectionPart) {
        return new ReconnectTargetCommand(
                (NodeConnection)connectionPart.getModel(),
                (Node)getHost().getModel());
    }
}
