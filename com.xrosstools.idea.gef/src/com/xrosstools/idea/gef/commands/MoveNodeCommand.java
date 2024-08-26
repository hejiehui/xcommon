package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.model.Node;

import java.awt.*;

public class MoveNodeCommand extends Command {
    private Node node;
    private Rectangle oldConstraint;
    private Rectangle newConstraint;

    public void setConstraint(Rectangle c) {
    	newConstraint = c;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void execute() {
    	oldConstraint = new Rectangle();
    	oldConstraint.setLocation(node.getLocation());
    	oldConstraint.setSize(node.getSize());
        node.setLocation(newConstraint.getLocation());
        node.setSize(newConstraint.getSize());
    }

    public String getLabel() {
        return "Move Node";
    }

    public void redo() {
        node.setLocation(newConstraint.getLocation());
        node.setSize(newConstraint.getSize());
    }

    public void undo() {
        node.setLocation(oldConstraint.getLocation());
        node.setSize(oldConstraint.getSize());
    }
}
