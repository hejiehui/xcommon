package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.NodeConnection;

public class DeleteConnectionCommand extends Command {
	private NodeConnection connection;

	public DeleteConnectionCommand init(NodeConnection connection){
		this.connection = connection;
		return this;
	}

    public NodeConnection getConnection(){
        return connection;
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
