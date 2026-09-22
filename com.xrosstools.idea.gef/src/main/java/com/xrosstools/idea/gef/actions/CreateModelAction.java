package com.xrosstools.idea.gef.actions;

import com.xrosstools.idea.gef.core.EditorInteraction;

import java.awt.event.ActionEvent;

public class CreateModelAction extends Action{
    private Class modelClass;
    private EditorInteraction editorInteraction;

    public CreateModelAction(EditorInteraction editorInteraction, Class modelClass) {
        this.editorInteraction = editorInteraction;
        this.modelClass = modelClass;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            Object node = modelClass.newInstance();
            editorInteraction.createModel(node);
        } catch (InstantiationException | IllegalAccessException e1) {
            throw new IllegalArgumentException(modelClass.getCanonicalName());
        }
    }
}
