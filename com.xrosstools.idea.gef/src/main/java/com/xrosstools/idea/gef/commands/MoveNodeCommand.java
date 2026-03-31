package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.Node;

import java.awt.*;

public class MoveNodeCommand extends Command {
    private Node node;
    private Rectangle oldConstraint;
    private Rectangle newConstraint;

    public MoveNodeCommand init(Node node, Rectangle c) {
        this.node = node;
    	newConstraint = c;
    	return this;
    }

    public void execute() {
    	oldConstraint = new Rectangle();
    	oldConstraint.setLocation(node.getLocation());
    	oldConstraint.setSize(node.getSize());
        redo();
    }

    public String getLabel() {
        return "Move Node";
    }

    public void redo() {
        node.setLocation(newConstraint.getLocation());
        node.setSize(newConstraint.getSize());
        postExecute();
    }

    public void undo() {
        node.setLocation(oldConstraint.getLocation());
        node.setSize(oldConstraint.getSize());
        postExecute();
    }

    public void postExecute() {}

    public Node getNode() {
        return node;
    }

    public Rectangle getOldConstraint() {
        return oldConstraint;
    }

    public Rectangle getNewConstraint() {
        return newConstraint;
    }
}
