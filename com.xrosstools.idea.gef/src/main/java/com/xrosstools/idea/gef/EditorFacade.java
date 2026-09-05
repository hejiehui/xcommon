package com.xrosstools.idea.gef;

import com.xrosstools.idea.gef.figures.Figure;
import com.xrosstools.idea.gef.util.IPropertySource;

import javax.swing.*;

public interface EditorFacade<T extends IPropertySource> {
    void setToolTipText(String tooltip);
    void updateRootFigure(Figure rootFigure);

    // For adjust editor panel
    void updateSelectedFigure(Figure selectedFigure);
    void updateSelectedModel(Object model);
    void updateFeedbackFigure(Figure feedbackFigure);

    // Notify all updates are done, refresh visual is required
    void refreshVisual();

    // Model is changed and needs save
    void save(T model);
    void showContextMenu(int x, int y, JPopupMenu menu);
}
