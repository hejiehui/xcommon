package com.xrosstools.idea.gef;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.xrosstools.idea.gef.actions.Action;
import com.xrosstools.idea.gef.actions.ActionContainer;
import com.xrosstools.idea.gef.core.ContentProvider;
import com.xrosstools.idea.gef.parts.AbstractTreeEditPart;
import com.xrosstools.idea.gef.parts.EditPart;
import com.xrosstools.idea.gef.parts.EditPartFactory;
import com.xrosstools.idea.gef.util.IPropertySource;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;

import static com.xrosstools.idea.gef.LspEditorFacade.IS_SEPARATOR;
import static com.xrosstools.idea.gef.LspEditorFacade.TOOLTIP;

public class PanelContentProviderAdapter<T extends IPropertySource> extends AbstractPanelContentProvider<T> {
    private ContentProvider<T> contentProvider;
    private T diagram;

    public PanelContentProviderAdapter(@NotNull VirtualFile virtualFile, ContentProvider<T> contentProvider) {
        super(virtualFile);
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
        for (Action action : contentProvider.getPaletteItems()) {
            palette.add(createPaletteButton(action, action.getIcon(), action.getTooltip()));
        }
    }

    @Override
    public ActionGroup createToolbar() {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (Action action : contentProvider.getToolbarItems()) {
            if(action == Action.SEPARATOR) {
                actionGroup.addSeparator();
            }

            actionGroup.add(createToolbarAction(action, action.getIcon(), action.getTooltip()));
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
