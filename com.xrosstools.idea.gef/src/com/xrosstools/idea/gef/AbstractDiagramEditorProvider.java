package com.xrosstools.idea.gef;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xrosstools.idea.gef.util.IPropertySource;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDiagramEditorProvider<T extends IPropertySource> implements FileEditorProvider, DumbAware {
    public abstract FileType getFileType();
    public abstract String getExtention();
    public abstract String getEditorTypeId();
    public abstract PanelContentProvider<T> createPanelContentProvider(@NotNull Project project, @NotNull VirtualFile virtualFile);

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (virtualFile.getFileType() == getFileType())
            return true;

        if (virtualFile.getExtension() == null)
            return false;

        return virtualFile.getExtension().equalsIgnoreCase(getExtention());
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return new DiagramEditor(getEditorTypeId(), createPanelContentProvider(project, virtualFile));
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
