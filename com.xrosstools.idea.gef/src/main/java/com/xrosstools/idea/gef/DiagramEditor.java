package com.xrosstools.idea.gef;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import com.xrosstools.idea.gef.util.IPropertySource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;

public class DiagramEditor<T extends IPropertySource> extends PsiTreeChangeAdapter implements FileEditor, FileEditorManagerListener, VirtualFileListener {
    private String name;
    private PanelContentProvider<T> contentProvider;
    private EditorPanel panel;
    private Project project;

    public DiagramEditor(Project project, String name, PanelContentProvider<T> contentProvider) {
        this.name = name;
        this.contentProvider = contentProvider;
        this.project = project;
        PsiManager.getInstance(project).addPsiTreeChangeListener(this);
        VirtualFileManager.getInstance().addVirtualFileListener(this);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        if(panel != null)
            return panel;

        try{
            panel = new EditorPanel<>(contentProvider);
        }catch(Throwable e) {
            throw new IllegalArgumentException(e);
        }

        return panel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return panel;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    public void selectTopLevelElement(String idField, String name) {
        panel.selectTopLevelElement(idField, name);
    }

    private void refresh() {
        if(panel != null)
            panel.contentsChanged(project);
    }

    @Override
    public void contentsChanged(VirtualFileEvent event) {
        if(event.getFile().equals(contentProvider.getFile()))
            refresh();
    }

    @Override
    public void childReplaced(PsiTreeChangeEvent event) {
        if(event.getFile() == null || !event.getFile().getVirtualFile().equals(contentProvider.getFile()))
            return;

        refresh();
    }

    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        if(event.getFile() == null || !event.getFile().getVirtualFile().equals(contentProvider.getFile()))
            return;

        refresh();
    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
        if (!PsiTreeChangeEvent.PROP_FILE_NAME.equals(event.getPropertyName())) return;
        PsiElement element = event.getElement();
        if (!(element instanceof PsiNamedElement) || !element.isValid()) return;
        if (!(element instanceof PsiJavaFile)) return;

        PsiClass[] classes = ((PsiJavaFile)element).getClasses();
        if(classes.length == 0) return;

        refresh();
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void selectNotify() {
        if(panel != null)
            panel.repaint();
    }

    @Override
    public void deselectNotify() {
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {
    }

    @Nullable
    @Override
    public <K> K getUserData(@NotNull Key<K> key) {
        return null;
    }

    @Override
    public <K> void putUserData(@NotNull Key<K> key, @Nullable K t) {
    }


    @NotNull
    public VirtualFile getFile() {
        return contentProvider.getFile();
    }
}