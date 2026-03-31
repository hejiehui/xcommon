package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeConnection;

public class ReconnectSourceCommand extends Command {
	private NodeConnection connection;
	private Node oldSource;
	private Node newSource;

	public ReconnectSourceCommand init(NodeConnection connection, Node newSource){
        this.connection = connection;
        this.newSource = newSource;
        oldSource = connection.getSource();
        return this;
    }

	public String getLabel() {
		return "Reconnect source";
	}

	public boolean canExecute() {
		if(oldSource == newSource)
			return false;

		return connection.getTarget().isSourceAcceptable(newSource) && newSource.checkOutputLimit();
	}

	public void execute() {
		oldSource.removeOutput(connection);
		connection.setSource(newSource);
		newSource.addOutput(connection);
		postExecute();
	}

	public void undo() {
		newSource.removeOutput(connection);
		connection.setSource(oldSource);
		oldSource.addOutput(connection);
		postExecute();
	}

	public void postExecute() {}

	public NodeConnection getConnection() {
		return connection;
	}

	public Node getOldSource() {
		return oldSource;
	}

	public Node getNewSource() {
		return newSource;
	}
}