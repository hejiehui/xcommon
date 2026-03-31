package com.xrosstools.idea.gef.util;

public class DataTypePropertyDescriptor extends ListPropertyDescriptor {
    public DataTypePropertyDescriptor(String label) {
        super(label, DataTypeEnum.values());
    }

    @Override
    public Object convertToProperty(String displayText) {
        return DataTypeEnum.findByDisplayName(displayText);
    }

    public String getDisplayText(Object editorValue) {
        return ((DataTypeEnum)editorValue).getDisplayName();
    }
}
