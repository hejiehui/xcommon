package com.xrosstools.idea.gef.actions;

import com.intellij.openapi.project.Project;
import com.xrosstools.idea.gef.commands.Command;
import com.xrosstools.idea.gef.commands.PropertyChangeCommand;
import com.xrosstools.idea.gef.util.IPropertySource;

public class ChangeImplementationAction extends Action implements ImplementationMessages {
    private Project project;
    private IPropertySource source;
    private String propertyName;

    public ChangeImplementationAction(Project project, IPropertySource source, String propertyName){
        this(project, String.format(CHANGE_ACTION_MSG, propertyName), source, propertyName);
    }

    public ChangeImplementationAction(Project project, String message, IPropertySource source, String propertyName){
        setText(message);
        this.project = project;
        this.source = source;
        this.propertyName = propertyName;
    }

    public Command createCommand() {
        String impl = ImplementationUtil.assignImpl(project, "");

        if(impl == null || impl.equals(""))
            return null;

        return new PropertyChangeCommand(source, propertyName, impl);
    }
}
