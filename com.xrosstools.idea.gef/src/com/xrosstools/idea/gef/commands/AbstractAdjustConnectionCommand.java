package com.xrosstools.idea.gef.commands;

import com.xrosstools.idea.gef.parts.AbstractConnectionEditPart;

public abstract class AbstractAdjustConnectionCommand<Constraint> extends Command {
    private AbstractConnectionEditPart connectionEditPart;
    private Constraint oldConstraint;
    private Constraint newConstraint;

    public AbstractAdjustConnectionCommand(AbstractConnectionEditPart connectionEditPart) {
        this.connectionEditPart = connectionEditPart;
        oldConstraint = getOldConstraint(connectionEditPart);
        newConstraint = getNewConstraint(connectionEditPart);
    }

    abstract public Constraint getOldConstraint(AbstractConnectionEditPart connectionEditPart);
    abstract public Constraint getNewConstraint(AbstractConnectionEditPart connectionEditPart);
    abstract public void setConstraint(AbstractConnectionEditPart connectionEditPart, Constraint constraint);

    public boolean canExecute() {
        return true;
    }

    public void execute() {
        redo();
    }

    public String getLabel() {
        return "Adjust connection";
    }

    public void redo() {
        setConstraint(connectionEditPart, newConstraint);
        postExecute();
    }

    public void undo() {
        setConstraint(connectionEditPart, oldConstraint);
        postExecute();
    }

    public void postExecute() {}

    public AbstractConnectionEditPart getonnectionEditPart() {
        return connectionEditPart;
    }
}
