package com.xrosstools.idea.gef.actions;

import com.xrosstools.idea.gef.ImplementationUtil;
import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.PropertyChangeCommand;
import com.xrosstools.idea.gef.util.IPropertySource;

public class ChangeMethodAction extends Action {
    public static final String ANY_METHODS = "*";

    private IPropertySource source;
    private String propertyName;
    private String methodName;

    public ChangeMethodAction(IPropertySource source, String propertyName, String methodName, boolean isPrivate){
        setText(isPrivate ? '-' + methodName : methodName);
        this.source = source;
        this.propertyName = propertyName;
        this.methodName = methodName;
        setChecked(methodName.equals(ImplementationUtil.getMethodName((String)source.getPropertyValue(propertyName))));
    }

    public ChangeMethodAction(IPropertySource source, String propertyName){
        this.source = source;
        this.propertyName = propertyName;
        this.methodName = ANY_METHODS;
    }

    public Command createCommand() {
        String impl = (String)source.getPropertyValue(propertyName);
        impl = ImplementationUtil.replaceMethodName(impl, methodName);
        return new PropertyChangeCommand(source, propertyName, impl);
    }
}
