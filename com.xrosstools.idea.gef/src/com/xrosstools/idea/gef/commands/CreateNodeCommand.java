package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeContainer;

import java.awt.*;

public class CreateNodeCommand extends Command {
    private NodeContainer nodeContainer;
    private Node node;
    private Point location;
    
    public CreateNodeCommand(
            NodeContainer nodeContainer,
            Node node,
    		Point location){
    	this.nodeContainer = nodeContainer;
    	this.node = node;
    	this.location = location;
    }

    @Override
    public boolean canExecute() {
        return nodeContainer.isChildAcceptable(node);
    }
    
    public void execute() {
        node.setLocation(location);
        nodeContainer.addChild(node);
    }

    public String getLabel() {
        return "Create node";
    }

    public void redo() {
        execute();
    }

    public void undo() {
        nodeContainer.removeChild(node);
    }
}
