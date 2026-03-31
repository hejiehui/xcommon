package com.xrosstools.idea.gef.util;

import com.intellij.openapi.ui.Messages;

public class FloatPropertyDescriptor extends NumberPropertyDescriptor {
    @Override
    public Object convertToProperty(String text) {
        try {
            return Float.parseFloat(text);
        }catch(NumberFormatException e) {
            Messages.showErrorDialog(text + " is not a valid float", "Data format error");
            return originalValue;
        }
    }
}
