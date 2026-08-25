package com.xrosstools.idea.gef;

import com.xrosstools.idea.gef.figures.Figure;

import javax.swing.*;

public class LspEditorFacade implements EditorFacade {
    private String tooltip;
    @Override
    public void setToolTipText(String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void updateRootFigure(Figure rootFigure) {

    }

    @Override
    public void updateSelectedFigure(Figure selectedFigure) {

    }

    @Override
    public void updateSelectedModel(Object model) {

    }

    @Override
    public void updateFeedbackFigure(Figure feedbackFigure) {

    }

    @Override
    public void refreshVisual() {

    }

    @Override
    public void save(Object model) {

    }

    @Override
    public void showContextMenu(int x, int y, JPopupMenu menu) {

    }
}
