package com.xrosstools.idea.gef.tools;

import com.xrosstools.idea.gef.GefIcons;
import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.core.EditorInteraction;

import java.awt.event.ActionEvent;

public class RedoAction extends Action {
    private EditorInteraction editorInteraction;

    public RedoAction(EditorInteraction editorInteraction) {
        setText("Redo");
        setTooltip("Redo");
        setIcon(GefIcons.Redo);
        this.editorInteraction = editorInteraction;
    }

    @Override
    public void actionPerformed(ActionEvent anActionEvent) {
        editorInteraction.redo();
    }

    public boolean calculateEnabled() {
        boolean enabled = editorInteraction.getCommandStack().canRedo();
        if(enabled)
            setText("Redo " + editorInteraction.getCommandStack().getRedoCommandLabel());
        return enabled;
    }
}
