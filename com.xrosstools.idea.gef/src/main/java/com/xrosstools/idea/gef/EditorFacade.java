package com.xrosstools.idea.gef;

import com.xrosstools.idea.gef.figures.Figure;

import javax.swing.*;

public interface EditorFacade<T> {
    void setToolTipText(String tooltip);
    void updateRootFigure(Figure rootFigure);
    void updateSelectedFigure(Figure selectedFigure);
    void updateSelectedModel(Object model);
    void updateFeedbackFigure(Figure feedbackFigure);
    void refreshVisual();
    void save(T model);
    void showContextMenu(int x, int y, JPopupMenu menu);
}
