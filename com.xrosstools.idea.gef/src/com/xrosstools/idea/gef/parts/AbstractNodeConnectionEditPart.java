package com.xrosstools.idea.gef.parts;

import com.xrosstools.idea.gef.policies.NodeConnectionEditPolicy;

public abstract class AbstractNodeConnectionEditPart extends AbstractConnectionEditPart {
    protected EditPolicy createEditPolicy() {
        return new NodeConnectionEditPolicy();
    }

    public void setSelected(int value) {
        if (value == AbstractGraphicalEditPart.SELECTED)
            getFigure().setLineWidth(2);
        else
            getFigure().setLineWidth(1);
    }
}
