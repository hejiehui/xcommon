package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeConnection;

public class CreateConnectionCommand extends Command {
	public static final int NO_LIIMIT = -1;

	private NodeConnection connection;
	private Node source;
	private Node target;

	private int sourceLimit;
	private int targetLimit;

    public CreateConnectionCommand(NodeConnection connection, Node source, Node target) {
        this.connection = connection;
        this.source = source;
        this.target = target;

		this.sourceLimit = source.getOutputLimit();
		this.targetLimit = target.getInputLimit();
    }

	public boolean canExecute() {
		if(!(source.isTargetAcceptable(target) && target.isSourceAcceptable(source)))
			return false;

		return source.checkOutputLimit() && target.checkInputLimit();
	}

	public void execute() {
        connection.setSource(source);
		connection.setTarget(target);
		redo();
    }

	public void redo() {
		source.addOutput(connection);
		target.addInput(connection);
	}

	public String getLabel() {
		return "Create connection";
	}

	public void undo() {
		source.removeOutput(connection);
		target.removeInput(connection);
	}
}