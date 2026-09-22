package com.xrosstools.idea.gef;

import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.core.ContentProvider;
import com.xrosstools.idea.gef.core.EditorInteraction;
import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;
import com.xrosstools.idea.gef.parts.EditPart;
import com.xrosstools.idea.gef.parts.EditPartFactory;
import com.xrosstools.idea.gef.util.IPropertySource;

/**
 * This is for EditorPanel to work with EditorInteraction
 * @param <T>
 */
public class ContentProviderAdapter<T extends IPropertySource> implements ContentProvider<T> {
    private PanelContentProvider<T> contentProvider;

    public ContentProviderAdapter(PanelContentProvider<T> contentProvider) {
        this.contentProvider = contentProvider;
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
    public Action[] getPaletteItems(EditorInteraction<T> editorInteraction) {
        // EditorInteraction will never use this
        return new Action[0];
    }

    @Override
    public Action[] getToolbarItems(EditorInteraction<T> editorInteraction) {
        // EditorInteraction will never use this
        return new Action[0];
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
    public Action[] getContextMenuItems(EditPart editPart) {
        contentProvider.getContextMenuProvider().buildDisplayMenu(editPart);
        // EditorPanel will call ContextMenuProvider.getLastMenu() later
        return new Action[0];
    }

    @Override
    public Action[] getOutlineContextMenuItems(AbstractTreeEditPart editPart) {
        // EditorPanel will never use this
        return new Action[0];
    }
}
