package com.xrosstools.idea.gef.tools;

import com.xrosstools.idea.gef.GefIcons;
import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.core.EditorInteraction;

import java.awt.event.ActionEvent;

public class UndoAction extends Action {
    public static final String UNDO = "Undo";
    private EditorInteraction editorInteraction;

    public UndoAction(EditorInteraction editorInteraction) {
        setText(UNDO);
        setTooltip(UNDO);
        setIconId(UNDO);
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
            setText(UNDO + " " + editorInteraction.getCommandStack().getUndoCommandLabel());
        return enabled;
    }
}
