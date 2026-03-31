package com.xrosstools.idea.gef.util;

import com.intellij.openapi.ui.Messages;

public class IntegerPropertyDescriptor extends NumberPropertyDescriptor {
    @Override
    public Object convertToProperty(String text) {
        try {
            return Integer.parseInt(text);
        }catch(NumberFormatException e) {
            Messages.showErrorDialog(text + " is not a valid integer", "Data format error");
            return originalValue;
        }
    }
}
