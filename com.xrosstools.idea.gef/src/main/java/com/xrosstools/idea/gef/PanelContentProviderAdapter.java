package com.xrosstools.idea.gef;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.core.ContentProvider;
import com.xrosstools.idea.gef.core.EditorInteraction;
import com.xrosstools.idea.gef.parts.EditPartFactory;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;
import java.awt.*;

public class PanelContentProviderAdapter<T extends IPropertySource> extends AbstractPanelContentProvider<T> {
    private ContentProvider<T> contentProvider;
    private Project project;
    private T diagram;

    public PanelContentProviderAdapter(Project project, VirtualFile virtualFile, ContentProvider<T> contentProvider) {
        super(virtualFile);
        this.project = project;
        this.contentProvider = contentProvider;
    }

    @Override
    public T getContent() throws Exception {
        Document document = FileDocumentManager.getInstance().getDocument(getFile());

        diagram = convert(document.getText());
        //diagramChanged();
        return diagram;
    }

    @Override
    public void saveContent() throws Exception {
        String contentStr = convert(diagram);
        getFile().setBinaryContent(contentStr.getBytes(getFile().getCharset()));
    }

    @Override
    public void buildPalette(JPanel palette) {
        for (Action action : contentProvider.getPaletteItems(getEditorPanel().getEditorInteraction())) {
            palette.add(createPaletteButton(action, GefIcons.getIcon(action.getIconId(), contentProvider.getClass()), action.getTooltip()));
        }
    }

    @Override
    public ActionGroup createToolbar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (Action action : contentProvider.getToolbarItems(getEditorPanel().getEditorInteraction())) {
            if(action == Action.SEPARATOR) {
                actionGroup.addSeparator();
            }

            actionGroup.add(createToolbarAction(action, GefIcons.getIcon(action.getIconId(), contentProvider.getClass()), action.getTooltip()));
        }
        return actionGroup;
    }

    @Override
    public ContextMenuProvider getContextMenuProvider() {
        return new ContextMenuProvider.ContentProviderContextMenuProvider(contentProvider, true);
    }

    @Override
    public ContextMenuProvider getOutlineContextMenuProvider() {
        return new ContextMenuProvider.ContentProviderContextMenuProvider(contentProvider, false);
    }

    // TODO check instance consistent
    @Override
    public EditPartFactory createEditPartFactory() {
        return contentProvider.createEditPartFactory();
    }

    @Override
    public EditPartFactory createTreePartFactory() {
        return contentProvider.createTreePartFactory();
    }

    @Override
    public T convert(String text) throws Exception {
        return contentProvider.convert(text);
    }

    @Override
    public String convert(T diagram) throws Exception {
        return contentProvider.convert(diagram);
    }

    public ContentProvider<T> getContentProvider() {
        return contentProvider;
    }
}
