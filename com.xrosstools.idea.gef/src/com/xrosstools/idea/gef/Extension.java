package com.xrosstools.idea.gef;

import com.intellij.openapi.actionSystem.ActionGroup;

public interface Extension {
    void setEditPanel(EditorPanel panel);
    void extendToolbar(ActionGroup toolbar);
}
