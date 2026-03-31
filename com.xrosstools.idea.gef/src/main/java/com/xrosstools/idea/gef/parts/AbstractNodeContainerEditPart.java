package com.xrosstools.idea.gef.parts;

import com.xrosstools.idea.gef.model.NodeContainer;

import java.util.List;

public abstract class AbstractNodeContainerEditPart extends AbstractGraphicalEditPart {
    public List getModelChildren() {
        return ((NodeContainer)getModel()).getChildren();
    }
}
