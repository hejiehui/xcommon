package com.xrosstools.idea.gef.parts;

import com.xrosstools.idea.gef.EditorPanel;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.util.Map;

public class EditContext {
    private EditorPanel contentPane;

    private Map<Icon, String> iconMap;

    private DefaultTreeModel treeModel;

    private AbstractGraphicalEditPart rootPart;

    private AbstractTreeEditPart treeRootPart;

    public EditContext() {}

    public EditContext(EditorPanel contentPane) {
        this.contentPane = contentPane;
    }

    public EditorPanel getContentPane() {
        return contentPane;
    }

    public void checkRootPart(EditPart parent, EditPart part) {
        if(parent != null) return;;

        if(rootPart == null && part instanceof AbstractGraphicalEditPart )
            rootPart = (AbstractGraphicalEditPart)part;

        if(treeRootPart == null && part instanceof AbstractTreeEditPart )
            treeRootPart = (AbstractTreeEditPart)part;
    }

    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    public AbstractGraphicalEditPart findEditPart(Object model) {
        return rootPart.findEditPart(model);
    }

    public AbstractTreeEditPart findTreeEditPart(Object model) {
        return (AbstractTreeEditPart)treeRootPart.findEditPart(model);
    }

    public void setTreeModel(DefaultTreeModel treeModel) {
        this.treeModel = treeModel;
    }


    public String getIconId(Icon icon) {
        return iconMap.get(icon);
    }

    public void setIconMap(Map<Icon, String> iconMap) {
        this.iconMap = iconMap;
    }
}
