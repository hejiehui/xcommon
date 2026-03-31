package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeConnection;
import com.xrosstools.idea.gef.model.NodeContainer;

public class DeleteNodeCommand extends Command {
    private NodeContainer nodeContainer;
    private Node node;
    private int index;
    
    public DeleteNodeCommand init(
            NodeContainer diagram,
            Node node){
    	this.nodeContainer = diagram;
    	this.node = node;
        index = nodeContainer.inexOf(node);
    	return this;
    }
    
    public void execute() {
        nodeContainer.removeChild(node);

        for(Object conn: node.getOutputs()){
            NodeConnection path = (NodeConnection)conn;
        	path.getTarget().getInputs().remove(path);
        }

        for(Object conn: node.getInputs()){
            NodeConnection path = (NodeConnection)conn;
            path.getSource().getOutputs().remove(path);
        }
    }

    public String getLabel() {
        return "Delete Node";
    }

    public void redo() {
        execute();
    }

    public void undo() {
        nodeContainer.addChild(index, node);

        for(Object conn: node.getOutputs()){
            NodeConnection path = (NodeConnection)conn;
            path.getTarget().getInputs().add(path);
        }

        for(Object conn: node.getInputs()){
            NodeConnection path = (NodeConnection)conn;
            path.getSource().getOutputs().add(path);
        }
    }

    public NodeContainer getNodeContainer() {
        return nodeContainer;
    }

    public Node getNode() {
        return node;
    }
}
