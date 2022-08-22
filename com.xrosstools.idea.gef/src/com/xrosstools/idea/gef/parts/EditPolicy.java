package com.xrosstools.idea.gef.parts;

import com.xrosstools.idea.gef.commands.Command;

import java.awt.*;

/**
 * If any operation is not applicable, just return null
 */
public class EditPolicy {
    private AbstractGraphicalEditPart host;

    public AbstractGraphicalEditPart getHost() {
        return host;
    }

    public void setHost(AbstractGraphicalEditPart host) {
        this.host = host;
    }

    /**
     * Delete current element
     */
    public Command getDeleteCommand() {return null;}

    /**
     * Add a newly created element
     */
    public Command getCreateCommand(Object newModel, Point location) {return null;}

    /**
     * Move an element from elsewhere
     */
    public Command getMoveCommand(AbstractGraphicalEditPart child, Rectangle constraint) {return null;}

    /**
     * Change size and/or location
     */
    public Command getChangeCommand(Rectangle constraint) {return null;}

    public boolean isSelectableSource(Object connectionModel) {return false;}

    public Command getCreateConnectionCommand(Object newConnectionModel, AbstractGraphicalEditPart sourcePart) {return null;}

    public Command getReconnectSourceCommand(AbstractConnectionEditPart connectionPart) {return null;}

    public Command getReconnectTargetCommand(AbstractConnectionEditPart connectionPart) {return null;}
}
