package com.xrosstools.idea.gef.util;

import com.xrosstools.idea.gef.routers.RouterStyle;

public class RouterStylePropertyDescriptor extends ListPropertyDescriptor {
    public RouterStylePropertyDescriptor(String label) {
        super(label, RouterStyle.values());
    }

    @Override
    public Object convertToProperty(String displayText) {
        return RouterStyle.findByDisplayName(displayText);
    }

    public String getDisplayText(Object editorValue) {
        return ((RouterStyle)editorValue).getDisplayName();
    }
}
