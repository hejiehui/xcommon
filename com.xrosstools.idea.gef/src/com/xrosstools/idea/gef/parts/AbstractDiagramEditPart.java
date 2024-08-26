package com.xrosstools.idea.gef.parts;

import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeContainer;
import com.xrosstools.idea.gef.policies.NodeContainerEditPolicy;

import java.util.List;

public abstract class AbstractDiagramEditPart extends AbstractGraphicalEditPart {
    public List<Node> getModelChildren() {
        return ((NodeContainer)getModel()).getChildren();
    }

    protected EditPolicy createEditPolicy() {
        return new NodeContainerEditPolicy();
    }
}
