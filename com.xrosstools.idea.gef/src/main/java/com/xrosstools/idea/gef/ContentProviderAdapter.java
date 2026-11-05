package com.xrosstools.idea.gef;

import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.gef.ContentProvider;
import com.xrosstools.gef.EditorInteraction;
import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;
import com.xrosstools.idea.gef.parts.EditPart;
import com.xrosstools.idea.gef.parts.EditPartFactory;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;
import java.util.List;
import java.util.Map;

/**
 * This is for EditorPanel to work with EditorInteraction
 * @param <T>
 */
public class ContentProviderAdapter<T extends IPropertySource> implements ContentProvider<T> {
    private PanelContentProvider<T> contentProvider;

    public void setEditorInteraction(EditorInteraction<T> editorInteraction){}
    public EditorInteraction<T> getEditorInteraction(){return null;}

    public ContentProviderAdapter(PanelContentProvider<T> contentProvider) {
        this.contentProvider = contentProvider;
    }

    @Override
    public Map<Icon, String> getIconMap() {
        // EditorInteraction will never use this
        return Map.of();
    }

    @Override
    public T convert(String text) throws Exception {
        return contentProvider.convert(text);
    }

    @Override
    public String convert(T diagram) throws Exception {
        return contentProvider.convert(diagram);
    }

    @Override
    public List<Action> getPaletteItems() {
        // EditorInteraction will never use this
        return null;
    }

    @Override
    public List<Action> getToolbarItems() {
        // EditorInteraction will never use this
        return null;
    }

    @Override
    public EditPartFactory createEditPartFactory() {
        return contentProvider.createEditPartFactory();
    }

    @Override
    public EditPartFactory createTreePartFactory() {
        return contentProvider.createTreePartFactory();
    }

    @Override
    public List<Action> getContextMenu(EditPart editPart) {
        contentProvider.getContextMenuProvider().buildDisplayMenu(editPart);
        // EditorPanel will call ContextMenuProvider.getLastMenu() later
        return null;
    }

    @Override
    public List<Action> getOutlineContextMenu(AbstractTreeEditPart editPart) {
        // EditorPanel will never use this
        return null;
    }
}
