package com.xrosstools.idea.gef.parts;

import com.xrosstools.idea.gef.policies.NodeConnectionEditPolicy;

public abstract class AbstractNodeConnectionEditPart extends AbstractConnectionEditPart {
    protected EditPolicy createEditPolicy() {
        return new NodeConnectionEditPolicy();
    }

    public void setSelected(boolean selected) {
        super.setSelected(selected);
        getFigure().setLineWidth(selected ? 2 : 1);
    }
}
