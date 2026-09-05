package com.xrosstools.idea.gef.tools;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.xrosstools.idea.gef.GefIcons;
import com.xrosstools.idea.gef.control.EditorInteraction;
import org.jetbrains.annotations.NotNull;

public class UndoAction extends AnAction {
    private EditorInteraction editorInteraction;

    public UndoAction(EditorInteraction editorInteraction) {
        super("Undo", "Undo", GefIcons.Undo);
        this.editorInteraction = editorInteraction;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        editorInteraction.undo();
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(editorInteraction.getCommandStack().canUndo());
        if(presentation.isEnabled())
            presentation.setText("Undo " + editorInteraction.getCommandStack().getUndoCommandLabel());
    }
}
