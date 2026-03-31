package com.xrosstools.idea.gef.treeparts;

import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionTreePart extends AbstractTreeEditPart {
    private Icon icon;
    private String text;
    private Collection children;

    public CollectionTreePart(String category, Icon icon, Collection children) {
        this.icon = icon;
        this.text = category;
        this.children = children;
    }

    @Override
    public String getText() {
        return text;
    }

    public Icon getImage() {
        return icon;
    }

    public List getModelChildren() {
        return new ArrayList(children);
    }
}
