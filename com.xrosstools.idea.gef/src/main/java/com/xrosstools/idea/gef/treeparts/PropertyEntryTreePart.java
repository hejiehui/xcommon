package com.xrosstools.idea.gef.treeparts;

import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;
import com.xrosstools.idea.gef.util.PropertyEntry;

import javax.swing.*;

public class PropertyEntryTreePart extends AbstractTreeEditPart {
    private Icon icon;

    public String getText() {
        PropertyEntry entry = (PropertyEntry)getModel();
        return String.format("%s: %s", entry.getName(), String.valueOf(entry.get()));
    }

    public Icon getImage() {
        return icon;
    }

    public PropertyEntryTreePart(Icon icon) {
        this.icon = icon;
    }
}
