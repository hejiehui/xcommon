package com.xrosstools.idea.gef.actions;

import com.xrosstools.idea.gef.core.ContentProvider;
import com.xrosstools.idea.gef.core.EditorInteraction;

import java.awt.event.ActionEvent;

public class CreateConnectionAction extends Action {
    private Class connectionClass;
    private EditorInteraction editorInteraction;

    public CreateConnectionAction(EditorInteraction editorInteraction, Class connectionClass) {
        this.editorInteraction = editorInteraction;
        this.connectionClass = connectionClass;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            Object conn = connectionClass.newInstance();
            editorInteraction.createConnection(conn);
        } catch (InstantiationException | IllegalAccessException e1) {
            throw new IllegalArgumentException(connectionClass.getCanonicalName());
        }
    }
}
