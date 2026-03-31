package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.NodeContainer;

public class CreateOrderedNodeCommand extends Command {
    private NodeContainer nodeContainer;
    private Object node;
    private int index;

    public CreateOrderedNodeCommand init(
            NodeContainer nodeContainer,
            Object node,
            int index){
        this.nodeContainer = nodeContainer;
        this.node = node;
        this.index = index;
        return this;
    }

    public void execute() {
        nodeContainer.addChild(index, node);
        postExecute();
    }

    public String getLabel() {
        return "Create node";
    }

    public void redo() {
        execute();
    }

    public void undo() {
        nodeContainer.removeChild(node);
        postExecute();
    }

    public NodeContainer getNodeContainer() {
        return nodeContainer;
    }

    public Object getNode() {
        return node;
    }

    public int getIndex() {
        return index;
    }

    public void postExecute() {}
}
