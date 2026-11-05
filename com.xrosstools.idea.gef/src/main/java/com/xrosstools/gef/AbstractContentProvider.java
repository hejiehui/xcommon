package com.xrosstools.gef;

import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.actions.CreateConnectionAction;
import com.xrosstools.idea.gef.actions.CreateModelAction;
import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;
import com.xrosstools.idea.gef.parts.EditPart;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public abstract class AbstractContentProvider<T extends IPropertySource> implements ContentProvider<T> {
    private EditorInteraction<T> editorInteraction;

    private ContextMenuBuilder<T> contextMenuBuilder;
    private ContextMenuBuilder<T> outlineContextMenuBuilder;

    public ContextMenuBuilder<T> createContextMenuBuilder() {
        return new ContextMenuBuilder<>();
    }

    public ContextMenuBuilder<T> createOutlineContextMenuBuilder() {
        return new ContextMenuBuilder<>();
    }

    @Override
    public void setEditorInteraction(EditorInteraction<T> editorInteraction){
        this.editorInteraction = editorInteraction;
    }

    @Override
    public EditorInteraction<T> getEditorInteraction(){
        return editorInteraction;
    }

    public Action createConnectionAction(String name, final Class<?> connectionClass, Icon icon) {
        Action createAction = new CreateConnectionAction(editorInteraction, connectionClass);
        createAction.setText(name);
        createAction.setTooltip("Create " + name);
        createAction.setIcon(icon);
        return createAction;
    }

    public Action createModelAction(String name, final Class<?> modelClass, Icon icon) {
        Action createAction = new CreateModelAction(editorInteraction, modelClass);
        createAction.setText(name);
        createAction.setTooltip("Create " + name);
        createAction.setIcon(icon);
        return createAction;
    }

    @Override
    public List<Action> getToolbarItems() {
        return Collections.emptyList();
    }

    public List<Action> getContextMenu(EditPart editPart) {
        if (contextMenuBuilder == null) {
            contextMenuBuilder = createContextMenuBuilder();
            editorInteraction.register(contextMenuBuilder);
        }

        return contextMenuBuilder.buildContextMenu(editPart);
    }

    public List<Action> getOutlineContextMenu(AbstractTreeEditPart editPart) {
        if (outlineContextMenuBuilder == null) {
            outlineContextMenuBuilder = createOutlineContextMenuBuilder();
            editorInteraction.register(outlineContextMenuBuilder);
        }

        return outlineContextMenuBuilder.buildContextMenu(editPart);
    }

    public <K extends ContentChangeListener<T>> K register(K listener) {
        editorInteraction.register(listener);
        return listener;
    }
}
