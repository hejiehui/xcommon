package com.xrosstools.idea.gef.core;

import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.actions.CreateConnectionAction;
import com.xrosstools.idea.gef.actions.CreateModelAction;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;

public abstract class AbstractContentProvider<T extends IPropertySource> implements ContentProvider<T> {
    public Action createConnectionAction(EditorInteraction<T> editorInteraction, String name, final Class<?> connectionClass, Icon icon) {
        Action createAction = new CreateConnectionAction(editorInteraction, connectionClass);
        createAction.setText(name);
        createAction.setTooltip("Create " + name);
        createAction.setIcon(icon);
        return createAction;
    }

    public Action createModelAction(EditorInteraction<T> editorInteraction, String name, final Class<?> modelClass, Icon icon) {
        Action createAction = new CreateModelAction(editorInteraction, modelClass);
        createAction.setText(name);
        createAction.setTooltip("Create " + name);
        createAction.setIcon(icon);
        return createAction;
    }

    @Override
    public Action[] getToolbarItems(EditorInteraction<T> editorInteraction) {
        return new Action[0];
    }
}
