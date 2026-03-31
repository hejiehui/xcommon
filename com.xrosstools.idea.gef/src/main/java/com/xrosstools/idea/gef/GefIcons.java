package com.xrosstools.idea.gef;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface GefIcons {
    Icon Undo = IconLoader.getIcon("/icons/undo.png", GefIcons.class);
    Icon Redo = IconLoader.getIcon("/icons/redo.png", GefIcons.class);

    Icon SEARCH = IconLoader.getIcon("/icons/search.png", GefIcons.class);
    Icon EXPORT_PDF = IconLoader.getIcon("/icons/export_pdf.png", GefIcons.class);
}
