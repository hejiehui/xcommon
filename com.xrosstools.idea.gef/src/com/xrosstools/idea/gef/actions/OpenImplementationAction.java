package com.xrosstools.idea.gef.actions;

import com.intellij.openapi.project.Project;
import com.xrosstools.idea.gef.commands.Command;

import java.awt.event.ActionEvent;

public class OpenImplementationAction extends Action implements ImplementationMessages {
    private Project project;
    private String implementation;
    public OpenImplementationAction(Project project, String propertyName, String implementation){
        setText(String.format(OPEN_ACTION_MSG, propertyName));
        this.project = project;
        this.implementation = implementation;
    }

    public void actionPerformed(ActionEvent e) {
        ImplementationUtil.openImpl(project, implementation);
    }

    public Command createCommand() {return null;}
}
