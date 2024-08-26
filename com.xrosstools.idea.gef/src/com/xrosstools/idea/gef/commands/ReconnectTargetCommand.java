package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeConnection;

public class ReconnectTargetCommand extends Command {
	private NodeConnection connection;
	private Node oldTarget;
	private Node newTarget;

	public ReconnectTargetCommand(NodeConnection connection, Node newTarget) {
		this.connection = connection;
		this.newTarget = newTarget;
		oldTarget = connection.getTarget();
	}
	
	public boolean canExecute() {
		if(oldTarget == newTarget)
			return false;

		return connection.getSource().isTargetAcceptable(newTarget) && newTarget.checkInputLimit();
	}

	public String getLabel() {
		return "Reconnect target";
	}

	public void execute() {
		oldTarget.removeInput(connection);
		connection.setTarget(newTarget);
		newTarget.addInput(connection);
	}

	public void undo() {
		newTarget.removeInput(connection);
		connection.setTarget(oldTarget);
		oldTarget.addInput(connection);
	}
}
