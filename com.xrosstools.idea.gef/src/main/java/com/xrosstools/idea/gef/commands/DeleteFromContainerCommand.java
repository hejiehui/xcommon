package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.NodeContainer;

public class DeleteFromContainerCommand<T> extends Command {
    private NodeContainer<T> nodeContainer;
    private T node;
    private int index;
    
    public DeleteFromContainerCommand init(
            NodeContainer<T> diagram,
            T node){
    	this.nodeContainer = diagram;
    	this.node = node;
    	index = nodeContainer.inexOf(node);
    	return this;
    }
    
    public void execute() {
        nodeContainer.removeChild(node);
    }

    public String getLabel() {
        return "Delete Node";
    }

    public void redo() {
        execute();
    }

    public void undo() {
        nodeContainer.addChild(index, node);
    }

    public NodeContainer<T> getNodeContainer() {
        return nodeContainer;
    }

    public T getNode() {
        return node;
    }
}
