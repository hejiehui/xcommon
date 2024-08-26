package com.xrosstools.idea.gef.util;

import com.intellij.openapi.ui.Messages;

public class LongPropertyDescriptor extends NumberPropertyDescriptor {
    @Override
    public Object convertToProperty(String text) {
        try {
            return Long.parseLong(text);
        }catch(NumberFormatException e) {
            Messages.showErrorDialog(text + " is not a valid long", "Data format error");
            return originalValue;
        }
    }
}
