package com.xrosstools.idea.gef;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.vfs.VirtualFile;
import com.xrosstools.idea.gef.parts.EditPartFactory;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;

public interface PanelContentProvider<T extends IPropertySource>{
    VirtualFile getFile();
    void setEditorPanel(EditorPanel editorPanel);
    EditorPanel getEditorPanel();

    //Read from virtual file
    T getContent() throws Exception;
    void saveContent() throws Exception;

    ContextMenuProvider getContextMenuProvider();
    ContextMenuProvider getOutlineContextMenuProvider();
    void buildPalette(JPanel palette);
    ActionGroup createToolbar();
    EditPartFactory createEditPartFactory();
    EditPartFactory createTreePartFactory();

    void preBuildRoot();
    void postBuildRoot();

    void createConnection(Object connModel);
    void createModel(Object model);

    default T convert(String text) throws Exception {
        return null;
    }

    default String convert(T diagram) throws Exception {
        return null;
    }
}