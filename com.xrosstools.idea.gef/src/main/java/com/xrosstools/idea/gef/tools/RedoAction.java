package com.xrosstools.idea.gef.tools;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.xrosstools.idea.gef.EditorPanel;
import com.xrosstools.idea.gef.GefIcons;
import com.xrosstools.idea.gef.control.EditorInteraction;
import org.jetbrains.annotations.NotNull;

public class RedoAction extends AnAction {
    private EditorInteraction editorInteraction;

    public RedoAction(EditorInteraction editorInteraction) {
        super("Redo", "Redo", GefIcons.Redo);
        this.editorInteraction = editorInteraction;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        editorInteraction.redo();
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(editorInteraction.getCommandStack().canRedo());
        if(presentation.isEnabled())
            presentation.setText("Redo " + editorInteraction.getCommandStack().getRedoCommandLabel());
    }
}
