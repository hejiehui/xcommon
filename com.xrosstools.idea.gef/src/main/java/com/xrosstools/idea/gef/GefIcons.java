package com.xrosstools.idea.gef;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.net.URL;

public interface GefIcons {
    Icon Undo = getIcon("undo", GefIcons.class);
    Icon Redo = getIcon("redo", GefIcons.class);

    Icon SEARCH = getIcon("search", GefIcons.class);
    Icon EXPORT_PDF = getIcon("export_pdf", GefIcons.class);

    static Icon getIcon(String iconId, Class<?> clazz) {
        try {
            URL url = clazz.getResource("/icons/" + iconId + ".png");
            return url != null ? new ImageIcon(url) : null;
        } catch (Throwable e) {
            return null;
        }
    }
}
