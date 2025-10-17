package com.xrosstools.idea.gef.extensions;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.xrosstools.idea.gef.EditorPanel;

public interface ToolbarExtension {
    void setEditPanel(EditorPanel panel);
    void extendToolbar(ActionGroup toolbar);
}
