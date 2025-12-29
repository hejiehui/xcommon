package com.xrosstools.idea.gef.tools;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.xrosstools.idea.gef.EditorPanel;
import com.xrosstools.idea.gef.GefIcons;
import org.jetbrains.annotations.NotNull;

public class RedoAction extends AnAction {
    private EditorPanel editorPanel;

    public RedoAction(EditorPanel editorPanel) {
        super("Redo", "Redo", GefIcons.Redo);
        this.editorPanel = editorPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        editorPanel.redo();
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(editorPanel.getCommandStack().canRedo());
        if(presentation.isEnabled())
            presentation.setText("Redo " + editorPanel.getCommandStack().getRedoCommandLabel());
    }
}
