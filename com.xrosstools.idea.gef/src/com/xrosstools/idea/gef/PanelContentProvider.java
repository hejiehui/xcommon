package com.xrosstools.idea.gef;

import com.intellij.openapi.vfs.VirtualFile;
import com.xrosstools.idea.gef.parts.EditContext;
import com.xrosstools.idea.gef.parts.EditPartFactory;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public interface PanelContentProvider<T extends IPropertySource> extends PropertyChangeListener {
    VirtualFile getFile();
    void setEditorPanel(EditorPanel editorPanel);
    EditorPanel getEditorPanel();

    T getContent() throws Exception;
    void saveContent() throws Exception;
    ContextMenuProvider getContextMenuProvider();
    ContextMenuProvider getOutlineContextMenuProvider();
    void buildPalette(JPanel palette);
    void buildToolbar(JToolBar toolbar);
    EditPartFactory createEditPartFactory();
    EditPartFactory createTreePartFactory();

    void preBuildRoot();
    void postBuildRoot();

    void createConnection(Object connModel);
    void createModel(Object model);
}