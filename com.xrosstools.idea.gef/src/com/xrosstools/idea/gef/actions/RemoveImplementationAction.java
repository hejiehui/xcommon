package com.xrosstools.idea.gef.actions;

import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.PropertyChangeCommand;
import com.xrosstools.idea.gef.util.IPropertySource;

public class RemoveImplementationAction extends Action implements ImplementationMessages {
    private IPropertySource source;
    private String propertyName;

    public RemoveImplementationAction(IPropertySource source, String propertyName){
        this(String.format(REMOVE_ACTION_MSG, propertyName), source, propertyName);
    }

    public RemoveImplementationAction(String message, IPropertySource source, String propertyName){
        setText(message);
        this.source = source;
        this.propertyName = propertyName;
    }

    public Command createCommand() {
        return new PropertyChangeCommand(source, propertyName, null);
    }
}
