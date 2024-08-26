package com.xrosstools.idea.gef.util;

import com.intellij.openapi.ui.Messages;

public class DoublePropertyDescriptor extends NumberPropertyDescriptor {
    @Override
    public Object convertToProperty(String text) {
        try {
            return Double.parseDouble(text);
        }catch(NumberFormatException e) {
            Messages.showErrorDialog(text + " is not a valid double", "Data format error");
            return originalValue;
        }
    }
}
