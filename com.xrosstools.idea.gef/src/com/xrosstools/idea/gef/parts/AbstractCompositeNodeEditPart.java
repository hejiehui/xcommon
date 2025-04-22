package com.xrosstools.idea.gef.parts;

import com.xrosstools.idea.gef.policies.NodeContainerEditPolicy;

@Deprecated
public abstract class AbstractCompositeNodeEditPart extends AbstractNodeContainerEditPart {
    protected EditPolicy createEditPolicy() {
        return new NodeContainerEditPolicy();
    }
}
