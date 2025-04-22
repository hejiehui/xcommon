package com.xrosstools.idea.gef.parts;

import com.xrosstools.idea.gef.policies.NodeContainerEditPolicy;

@Deprecated
public abstract class AbstractDiagramEditPart extends AbstractNodeContainerEditPart {
    protected EditPolicy createEditPolicy() {
        return new NodeContainerEditPolicy();
    }
}
