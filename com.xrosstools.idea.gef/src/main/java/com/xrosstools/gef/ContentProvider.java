package com.xrosstools.gef;

import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;
import com.xrosstools.idea.gef.parts.EditPart;
import com.xrosstools.idea.gef.parts.EditPartFactory;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public interface ContentProvider<T extends IPropertySource> {
    void setEditorInteraction(EditorInteraction<T> editorInteraction);
    EditorInteraction<T> getEditorInteraction();

    Map<Icon, String> getIconMap();

    T convert(String text) throws Exception;
    String convert(T diagram) throws Exception;

    List<Action> getPaletteItems();
    List<Action> getToolbarItems();

    EditPartFactory createEditPartFactory();
    EditPartFactory createTreePartFactory();

    List<Action> getContextMenu(EditPart editPart);
    List<Action> getOutlineContextMenu(AbstractTreeEditPart editPart);
}
