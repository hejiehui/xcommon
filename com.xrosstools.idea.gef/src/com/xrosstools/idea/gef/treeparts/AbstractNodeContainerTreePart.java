package com.xrosstools.idea.gef.treeparts;

import com.xrosstools.idea.gef.model.NodeContainer;
import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNodeContainerTreePart extends AbstractTreeEditPart {
    public AbstractNodeContainerTreePart(Object model) {
        super(model);
    }

    public List getModelChildren() {
        List children = new ArrayList();
        children.addAll(((NodeContainer)getModel()).getChildren());
        return children;
    }
}
