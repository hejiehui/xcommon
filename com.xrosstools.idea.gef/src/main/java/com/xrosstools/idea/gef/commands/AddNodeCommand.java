package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeContainer;

import java.awt.*;

public class AddNodeCommand extends Command {
    private NodeContainer oldContainer;
    private NodeContainer newContainer;
    private Node node;
    private Rectangle oldConstraint;
    private Rectangle newConstraint;
    private int oldIndex;

    public AddNodeCommand init(NodeContainer oldContainer, NodeContainer newContainer, Node node, Rectangle constraint) {
        this.oldContainer = oldContainer;
        this.newContainer = newContainer;
        this.node = node;
        this.newConstraint = constraint;
        return this;
    }

    public boolean canExecute() {
        return true;
    }

    public void execute() {
        oldIndex = oldContainer.inexOf(node);
        oldConstraint = new Rectangle();
        oldConstraint.setLocation(node.getLocation());
        oldConstraint.setSize(node.getSize());
        redo();
    }

    public String getLabel() {
        return "Move Node";
    }

    public void redo() {
        newContainer.addChild(node);
        node.setLocation(newConstraint.getLocation());
        node.setSize(newConstraint.getSize());
        postExecute();
    }

    public void undo() {
        oldContainer.addChild(oldIndex, node);
        node.setLocation(oldConstraint.getLocation());
        node.setSize(oldConstraint.getSize());
        postExecute();
    }

    public void postExecute() {}

    public NodeContainer getOldContainer() {
        return oldContainer;
    }

    public NodeContainer getNewContainer() {
        return newContainer;
    }

    public Node getNode() {
        return node;
    }

    public Rectangle getConstraint() {
        return newConstraint;
    }
}
