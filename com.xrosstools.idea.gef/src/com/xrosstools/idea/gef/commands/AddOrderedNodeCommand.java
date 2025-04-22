package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.NodeContainer;

public class AddOrderedNodeCommand extends Command {
    private Object node;
    private NodeContainer oldParent;
    private NodeContainer newParent;
    private int oldIndex;
    private int index;

    public AddOrderedNodeCommand init(NodeContainer oldParent, NodeContainer newParent, Object node, int index) {
        this.oldParent = oldParent;
        this.newParent = newParent;
        this.node = node;
        this.oldIndex = oldParent.inexOf(node);
        this.index = index;
        return this;
    }

    public void execute() {
        redo();
    }

    public String getLabel() {
        return "Move Node";
    }

    public void redo() {
        oldParent.removeChild(node);
        newParent.addChild(index, node);
        postExecute();
    }

    public void undo() {
        newParent.removeChild(node);
        oldParent.addChild(oldIndex, node);
        postExecute();
    }

    public void postExecute() {}

    public Object getNode() {
        return node;
    }

    public NodeContainer getOldParent() {
        return oldParent;
    }

    public NodeContainer getNewParent() {
        return newParent;
    }

    public int getOldIndex() {
        return oldIndex;
    }

    public int getIndex() {
        return index;
    }
}
