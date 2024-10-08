package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeContainer;

import java.awt.*;

public class CreateNodeCommand extends Command {
    private NodeContainer nodeContainer;
    private Node node;
    private Point location;
    
    public CreateNodeCommand init(
            NodeContainer nodeContainer,
            Node node,
    		Point location){
    	this.nodeContainer = nodeContainer;
    	this.node = node;
    	this.location = location;
    	return this;
    }

    @Override
    public boolean canExecute() {
        return nodeContainer.isChildAcceptable(node);
    }
    
    public void execute() {
        node.setLocation(location);
        nodeContainer.addChild(node);
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

    public Node getNode() {
        return node;
    }

    public Point getLocation() {
        return location;
    }

    public void postExecute() {}
}
