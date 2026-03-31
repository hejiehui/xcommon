package com.xrosstools.idea.gef.parts;

import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.policies.NodeEditPolicy;

import java.util.List;

public abstract class AbstractNodeEditPart extends AbstractGraphicalEditPart {
    @Override
    protected EditPolicy createEditPolicy() {
        return new NodeEditPolicy();
    }

    @Override
    public List getModelSourceConnections() {
        return ((Node)getModel()).getOutputs();
    }

    @Override
    public List getModelTargetConnections() {
        return ((Node)getModel()).getInputs();
    }
}
