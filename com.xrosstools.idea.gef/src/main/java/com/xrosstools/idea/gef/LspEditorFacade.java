package com.xrosstools.idea.gef;

public class LspEditorFacade implements EditorFacade {
    private String tooltip;
    @Override
    public void setToolTipText(String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void updateVisual() {

    }
}
