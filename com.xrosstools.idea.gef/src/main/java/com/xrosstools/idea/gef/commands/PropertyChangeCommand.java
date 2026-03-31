package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.util.IPropertySource;

public class PropertyChangeCommand extends Command {
    private IPropertySource source;
    private String category;
    private String propertyName;
    private Object oldValue;
    private Object newValue;

    public PropertyChangeCommand(IPropertySource source, String propertyName, Object newValue) {
        this(source, propertyName, source.getPropertyValue(propertyName), newValue);
    }

    public PropertyChangeCommand(IPropertySource source, String propertyName, Object oldValue, Object newValue) {
        this.source = source;
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public PropertyChangeCommand(IPropertySource source, String category, String propertyName, Object oldValue, Object newValue) {
        this(source, propertyName, oldValue, newValue);
        this.category = category;
    }

    public void execute() {
        if(category == null)
            this.source.setPropertyValue(propertyName, newValue);
        else
            this.source.setPropertyValue(category, propertyName, newValue);
    }

    public String getLabel() {
        return "Change property value";
    }

    public void redo() {
        execute();
    }

    public void undo() {
        if(category == null)
            this.source.setPropertyValue(propertyName, oldValue);
        else
            this.source.setPropertyValue(category, propertyName, oldValue);
    }
}
