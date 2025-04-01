package com.xrosstools.idea.gef.parts;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTreeEditPart extends AbstractEditPart {
    private DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(this);
    private List<AbstractTreeEditPart> childEditParts = new ArrayList<>();

    public AbstractTreeEditPart(Object model) {
        setModel(model);
    }

    public List getChildren() {
        return childEditParts;
    }

    protected DefaultTreeModel getTreeModel() {
        return getContext().getTreeModel();
    }

    protected void refreshVisuals() {
        getTreeModel().nodeChanged(treeNode);
    }

    @Override
    protected void addChildPartVisual(EditPart childEditPart, int index) {
        DefaultMutableTreeNode childNode = ((AbstractTreeEditPart) childEditPart).treeNode;
        DefaultMutableTreeNode childParentNode = (DefaultMutableTreeNode) childNode.getParent();
        if (childParentNode != null && childParentNode.getUserObject() != null && childParentNode.getUserObject() != this)
            getTreeModel().removeNodeFromParent(childNode);
        getTreeModel().insertNodeInto(childNode, treeNode, index);
    }

    @Override
    protected void removeChildVisual(EditPart childEditPart) {
        getTreeModel().removeNodeFromParent(((AbstractTreeEditPart) childEditPart).treeNode);
    }

    public DefaultMutableTreeNode getTreeNode() {
        return treeNode;
    }

    public String getText() {
        return "";
    }

    public abstract Icon getImage();

    protected EditPart createOrFindPart(Object model) {
        EditPart childEditPart = getContext().findTreeEditPart(model);
        if(childEditPart != null)
            return childEditPart;

        childEditPart = getEditPartFactory().createEditPart(getContext(), this, model);
        return childEditPart;
    }
}
