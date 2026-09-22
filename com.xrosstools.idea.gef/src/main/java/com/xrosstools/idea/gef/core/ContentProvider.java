package com.xrosstools.idea.gef.core;

import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;
import com.xrosstools.idea.gef.parts.EditPart;
import com.xrosstools.idea.gef.parts.EditPartFactory;
import com.xrosstools.idea.gef.util.IPropertySource;

public interface ContentProvider<T extends IPropertySource> {
    T convert(String text) throws Exception;
    String convert(T diagram) throws Exception;

    Action[] getPaletteItems(EditorInteraction<T> editorInteraction);
    Action[] getToolbarItems(EditorInteraction<T> editorInteraction);

    EditPartFactory createEditPartFactory();
    EditPartFactory createTreePartFactory();

    Action[] getContextMenuItems(EditPart editPart);
    Action[] getOutlineContextMenuItems(AbstractTreeEditPart editPart);
}
