package com.xrosstools.idea.gef.tools;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.xrosstools.idea.gef.EditorPanel;
import com.xrosstools.idea.gef.GefIcons;
import org.jetbrains.annotations.NotNull;

public class UndoAction extends AnAction {
    private EditorPanel editorPanel;

    public UndoAction(EditorPanel editorPanel) {
        super("Undo", "Undo", GefIcons.Undo);
        this.editorPanel = editorPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        editorPanel.undo();
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(editorPanel.getCommandStack().canUndo());
        if(presentation.isEnabled())
            presentation.setText("Undo " + editorPanel.getCommandStack().getUndoCommandLabel());
    }
}
