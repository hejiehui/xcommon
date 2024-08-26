package com.xrosstools.idea.gef.policies;

import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.DeleteConnectionCommand;
import com.xrosstools.idea.gef.model.NodeConnection;
import com.xrosstools.idea.gef.parts.EditPolicy;

public class NodeConnectionEditPolicy extends EditPolicy {

    public Command getDeleteCommand() {
        return new DeleteConnectionCommand((NodeConnection) getHost().getModel());
    }
}