package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.util.IPropertySource;

public class PropertyChangeCommand extends Command {
    private IPropertySource source;
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

    public void execute() {
        this.source.setPropertyValue(propertyName, newValue);
    }

    public String getLabel() {
        return "Change property value";
    }

    public void redo() {
        execute();
    }

    public void undo() {
        this.source.setPropertyValue(propertyName, oldValue);
    }
}
