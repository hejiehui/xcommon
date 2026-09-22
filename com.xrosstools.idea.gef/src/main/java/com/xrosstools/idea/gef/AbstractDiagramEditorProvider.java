package com.xrosstools.idea.gef;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xrosstools.idea.gef.core.ContentProvider;
import com.xrosstools.idea.gef.util.IPropertySource;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDiagramEditorProvider<T extends IPropertySource> implements FileEditorProvider, DumbAware {
    public abstract FileType getFileType();
    public abstract String getExtention();
    public abstract String getEditorTypeId();

    private Project project;
    private VirtualFile virtualFile;

    /**
     * For backward compatible. The old version implements it to provide old version of PanelContentProvider.
     * The new version will not implement it and will implement createPanelContentProvider instead.
     * The default logic is to wrap ContentProvider into PanelContentProvider to work with EditPanel
     */
    @Deprecated
    public PanelContentProvider<T> createPanelContentProvider(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return null;
    }

    /**
     * New subclass should override this method to provide ContentProvider.
     * For old version, it will provide ContentProvider wrapper for PanelContentProvider to make it backward compatible
     */
    public ContentProvider<T> createContentProvider() {
        return null;
    }

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
        this.project = project;
        this.virtualFile = virtualFile;

        PanelContentProvider<T> panelContentProvider = null;

        ContentProvider<T> contentProvider = createContentProvider();
        if(contentProvider != null) {
            panelContentProvider = new PanelContentProviderAdapter<T>(project, virtualFile, contentProvider);
        } else
            panelContentProvider = createPanelContentProvider(project, virtualFile);

        return new DiagramEditor<T>(project, getEditorTypeId(), panelContentProvider);
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }


}
