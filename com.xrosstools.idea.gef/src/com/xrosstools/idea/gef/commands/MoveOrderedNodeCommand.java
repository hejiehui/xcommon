package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeContainer;

public class MoveOrderedNodeCommand extends Command {
    private NodeContainer parent;
    private Object node;
    private int oldIndex;
    private int index;

    public MoveOrderedNodeCommand init(NodeContainer parent, Object node, int index) {
        this.parent = parent;
        this.node = node;
        this.oldIndex = parent.inexOf(node);
        oldIndex = oldIndex > index ? oldIndex + 1 : oldIndex;
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
        parent.move(index, node);
        postExecute();
    }

    public void undo() {
        parent.move(oldIndex, node);
        postExecute();
    }

    public void postExecute() {}

    public Object getNode() {
        return node;
    }

    public NodeContainer getParent() {
        return parent;
    }

    public int getOldIndex() {
        return oldIndex;
    }

    public int getIndex() {
        return index;
    }
}
