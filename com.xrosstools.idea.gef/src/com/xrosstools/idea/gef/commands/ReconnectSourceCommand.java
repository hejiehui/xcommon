package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeConnection;

public class ReconnectSourceCommand extends Command {
	private NodeConnection connection;
	private Node oldSource;
	private Node newSource;

	public ReconnectSourceCommand(NodeConnection connection, Node newSource){
        this.connection = connection;
        this.newSource = newSource;
        oldSource = connection.getSource();
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
	}

	public void undo() {
		newSource.removeOutput(connection);
		connection.setSource(oldSource);
		oldSource.addOutput(connection);
	}
}