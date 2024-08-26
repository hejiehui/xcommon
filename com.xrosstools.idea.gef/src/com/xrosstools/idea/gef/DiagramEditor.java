package com.xrosstools.idea.gef;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.xrosstools.idea.gef.util.IPropertySource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public class DiagramEditor<T extends IPropertySource> extends PsiTreeChangeAdapter implements FileEditor, FileEditorManagerListener, VirtualFileListener {
    private String name;
    private PanelContentProvider<T> contentProvider;
    private EditorPanel panel;

    public DiagramEditor(Project project, String name, PanelContentProvider<T> contentProvider) {
        this.name = name;
        this.contentProvider = contentProvider;
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
            e.printStackTrace(System.err);
            return new JLabel("Failed to load model File: " + e);
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

    private void refresh() {
        panel.contentsChanged();
    }

    @Override
    public void contentsChanged(VirtualFileEvent event) {
        if(event.getFile() == contentProvider.getFile())
            refresh();
    }

    @Override
    public void childReplaced(PsiTreeChangeEvent event) {
        if(event.getFile() == null || event.getFile().getVirtualFile() != contentProvider.getFile())
            return;

        PsiElement oldChild = event.getOldChild();
        PsiElement newChild = event.getNewChild();

        if (oldChild instanceof PsiIdentifier && newChild instanceof PsiIdentifier) {
            PsiIdentifier oldMethod = (PsiIdentifier) oldChild;
            PsiIdentifier newMethod = (PsiIdentifier) newChild;

            if (!oldMethod.getText().equals(newMethod.getText())) {
                FileDocumentManager.getInstance().saveAllDocuments();
            }
        }
    }

    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
        if(event.getFile() == null || event.getFile().getVirtualFile() != contentProvider.getFile())
            return;

        FileDocumentManager.getInstance().saveAllDocuments();
    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
        if (PsiTreeChangeEvent.PROP_FILE_NAME.equals(event.getPropertyName())) {
            PsiElement element = event.getElement();
            if (element instanceof PsiNamedElement && element.isValid()) {
                if(element instanceof PsiJavaFile) {
                    PsiClass[] classes = ((PsiJavaFile)element).getClasses();
                    if(classes.length > 0) {
                        FileDocumentManager.getInstance().saveAllDocuments();
                        refresh();
                    }
                }
            }
        }
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