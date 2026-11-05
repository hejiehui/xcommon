package com.xrosstools.idea.gef.actions;

import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.PropertyChangeCommand;
import com.xrosstools.idea.gef.util.IPropertySource;

import java.awt.event.ActionEvent;

public class ChangeImplementationAction extends Action implements ImplementationMessages {
    private IPropertySource source;
    private String propertyName;
    private String inputValue;

    public ChangeImplementationAction(String messageTemplate, IPropertySource source, String propertyName){
        setText(String.format(messageTemplate, propertyName));
        this.source = source;
        this.propertyName = propertyName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String curImpl = source.getPropertyValue(propertyName) == null ? "" : source.getPropertyValue(propertyName).toString();
        inputValue = getPlatformFacade().assignImpl(curImpl);
        super.actionPerformed(e);
    }

    public Command createCommand() {
        if(inputValue == null || inputValue.isEmpty())
            return null;

        return new PropertyChangeCommand(source, propertyName, inputValue);
    }
}
