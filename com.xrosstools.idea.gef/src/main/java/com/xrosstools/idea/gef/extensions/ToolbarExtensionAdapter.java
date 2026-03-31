package com.xrosstools.idea.gef.extensions;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.Tree;
import com.xrosstools.idea.gef.EditorPanel;
import com.xrosstools.idea.gef.parts.AbstractGraphicalEditPart;
import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;

public class ToolbarExtensionAdapter implements ToolbarExtension {
    private EditorPanel editorPanel;

    @Override
    public void extendToolbar(ActionGroup toolbar) {
    }

    @Override
    public void setEditPanel(EditorPanel editorPanel) {
        this.editorPanel = editorPanel;
    }

    public EditorPanel getEditorPanel() {
        return editorPanel;
    }

    public AbstractGraphicalEditPart getRoot() {
        return editorPanel.getRoot();
    }

    public AbstractTreeEditPart getTreeRoot() {
        return editorPanel.getTreeRoot();
    }

    public Tree getTreeNavigator() {
        return editorPanel.getTreeNavigator();
    }

    public JBTable getTableProperties() {
        return editorPanel.getTableProperties();
    }

}
