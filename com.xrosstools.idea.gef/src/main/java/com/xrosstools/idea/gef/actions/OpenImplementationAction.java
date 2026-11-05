package com.xrosstools.idea.gef.actions;

import java.awt.event.ActionEvent;

public class OpenImplementationAction extends Action implements ImplementationMessages {
    private String implementation;

    public OpenImplementationAction(String propertyName, String implementation){
        setText(String.format(OPEN_ACTION_MSG, propertyName));
        this.implementation = implementation;
    }

    public void actionPerformed(ActionEvent e) {
        getPlatformFacade().openImpl(implementation);
    }
}
