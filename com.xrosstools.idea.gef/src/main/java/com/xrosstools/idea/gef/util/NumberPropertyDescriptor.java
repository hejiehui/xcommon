package com.xrosstools.idea.gef.util;

public class NumberPropertyDescriptor extends TextPropertyDescriptor {
    protected Object originalValue;
    public String getDisplayText(Object editorValue) {
        originalValue = editorValue;
        return editorValue == null ? "" : editorValue.toString();
    }
}
