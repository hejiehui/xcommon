package com.xrosstools.idea.gef.policies;

import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.CreateNodeCommand;
import com.xrosstools.idea.gef.commands.MoveNodeCommand;
import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeContainer;
import com.xrosstools.idea.gef.parts.AbstractGraphicalEditPart;
import com.xrosstools.idea.gef.parts.EditPolicy;

import java.awt.*;

public class NodeContainerEditPolicy extends EditPolicy {
    public Command getCreateCommand(Object newModel, Point location) {
        return createCreateNodeCommand().init(
                (NodeContainer) getHost().getModel(),
                (Node) newModel,
                location);
    }

    public Command getMoveCommand(AbstractGraphicalEditPart child, Rectangle constraint) {
        if(!(child.getModel() instanceof Node))
            return null;

        return createMoveNodeCommand().init((Node) child.getModel(), constraint);
    }

    public MoveNodeCommand createMoveNodeCommand() {
        return new MoveNodeCommand();
    }

    public CreateNodeCommand createCreateNodeCommand() {
        return new CreateNodeCommand();
    }
}
