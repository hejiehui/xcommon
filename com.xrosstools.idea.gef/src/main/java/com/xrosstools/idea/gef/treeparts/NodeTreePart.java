package com.xrosstools.idea.gef.treeparts;

import com.xrosstools.idea.gef.model.Node;
import com.xrosstools.idea.gef.model.NodeConnection;
import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class NodeTreePart extends AbstractTreeEditPart {
    private boolean outputsAsChildren;

    @Deprecated
    public NodeTreePart(Object model) {
        this(model, false);
    }

    @Deprecated
    public NodeTreePart(Object model, boolean outputsAsChildren) {
        super(model);
        this.outputsAsChildren = outputsAsChildren;
    }

    public NodeTreePart(boolean outputsAsChildren) {
        this.outputsAsChildren = outputsAsChildren;
    }

    public List getModelChildren() {
        if(outputsAsChildren == false)
            return Collections.emptyList();

        List<Node> chidren = new ArrayList<>();
        for(Object connection : ((Node)getModel()).getOutputs())
            chidren.add(((NodeConnection)connection).getTarget());
        return chidren;
    }
}
