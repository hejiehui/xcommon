package com.xrosstools.idea.gef.parts;

import com.xrosstools.idea.gef.EditorPanel;

import javax.swing.tree.DefaultTreeModel;

public class EditContext {
    private EditorPanel contentPane;

    private AbstractGraphicalEditPart rootPart;

    private AbstractTreeEditPart treeRootPart;

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
        return contentPane.getTreeModel();
    }

    public AbstractGraphicalEditPart findEditPart(Object model) {
        return rootPart.findEditPart(model);
    }

    public AbstractTreeEditPart findTreeEditPart(Object model) {
        return (AbstractTreeEditPart)treeRootPart.findEditPart(model);
    }
}
