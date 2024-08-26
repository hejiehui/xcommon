package com.xrosstools.idea.gef.util;

import javax.swing.*;

public interface IPropertyDescriptor {
    void setId(Object id);
    Object getId();

    void setLabel(String labelk);
    String getLabel();

    void setCategory(String category);
    String getCategory();

    JComponent getEditor(Object value);

    Object getCellEditorValue();

    Object convertToProperty(String displayText);

    /**
     * @return if current property can be shown in property window.
     */
    boolean isVisible();

    void setVisible(Boolean visible);

    default String getDisplayText(Object editorValue) {
        return editorValue == null ? "" : editorValue.toString();
    }
}
