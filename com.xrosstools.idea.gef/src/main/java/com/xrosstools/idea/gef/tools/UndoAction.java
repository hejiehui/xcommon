package com.xrosstools.idea.gef.tools;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.xrosstools.idea.gef.GefIcons;
import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.core.EditorInteraction;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;

public class UndoAction extends Action {
    private EditorInteraction editorInteraction;

    public UndoAction(EditorInteraction editorInteraction) {
        setText("Undo");
        setTooltip("Undo");
        setIcon(GefIcons.Undo);
        this.editorInteraction = editorInteraction;
    }

    @Override
    public void actionPerformed(ActionEvent anActionEvent) {
        editorInteraction.undo();
    }

    public boolean calculateEnabled() {
        boolean enabled = editorInteraction.getCommandStack().canUndo();
        if(enabled)
            setText("Redo " + editorInteraction.getCommandStack().getUndoCommandLabel());
        return enabled;
    }
}
