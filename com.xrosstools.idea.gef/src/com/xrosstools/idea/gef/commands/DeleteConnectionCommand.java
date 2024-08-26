package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.model.NodeConnection;

public class DeleteConnectionCommand extends Command {
	private NodeConnection connection;
	public DeleteConnectionCommand(NodeConnection connection){
		this.connection = connection;
	}
	
    public void execute() {
        connection.getSource().removeOutput(connection);
        connection.getTarget().removeInput(connection);
    }

    public String getLabel() {
        return "Delete connection";
    }

    public void redo() {
        execute();
    }

    public void undo() {
        connection.getSource().addOutput(connection);
        connection.getTarget().addInput(connection);
    }
}
