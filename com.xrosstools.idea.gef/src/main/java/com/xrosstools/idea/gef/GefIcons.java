package com.xrosstools.idea.gef;

import com.xrosstools.idea.gef.tools.ExportPngAction;
import com.xrosstools.idea.gef.tools.RedoAction;
import com.xrosstools.idea.gef.tools.SearchModelAction;
import com.xrosstools.idea.gef.tools.UndoAction;

import javax.swing.*;
import java.net.URL;

public interface GefIcons {
    Icon Undo = getIcon(UndoAction.ID, GefIcons.class);
    Icon Redo = getIcon(RedoAction.ID, GefIcons.class);

    Icon SEARCH = getIcon(SearchModelAction.ID, GefIcons.class);
    Icon EXPORT_PNG = getIcon(ExportPngAction.ID, GefIcons.class);

    static Icon getIcon(String iconId, Class<?> clazz) {
        try {
            URL url = clazz.getResource("/icons/" + iconId + ".png");
            return url != null ? new ImageIcon(url) : null;
        } catch (Throwable e) {
            return null;
        }
    }
}
