package com.xrosstools.idea.gef.figures;

import com.xrosstools.idea.gef.parts.AbstractGraphicalEditPart;

import java.awt.*;

public class Endpoint extends Figure {
    public static final int SIZE = 8;

    public Endpoint() {
        setWidth(SIZE);
        setHeight(SIZE);
    }

    @Override
    public AbstractGraphicalEditPart getPart() {
        return getParent().getPart();
    }

    public Connection getParentConnection() {
        return (Connection)getParent();
    }

    public boolean isConnectionEndpoint() {
        return getParentConnection() instanceof  Connection;
    }

    public boolean isConnectionSourceEndpoint() {
        return isConnectionEndpoint() && getParentConnection().getSourceEndpoint() == this;
    }

    public boolean isConnectionTargetEndpoint() {
        return isConnectionEndpoint() && getParentConnection().getTargetEndpoint() == this;
    }

    @Override
    public boolean isSelectable() {
        //If connection has no edit policy, then end point should not be selectable
        return getPart() != null && getPart().getEditPolicy() != null;
    }

    @Override
    public void paint(Graphics graphics) {
        if(isSelectable() && getParentConnection().isSelected())
            graphics.fill3DRect(getX(), getY(), SIZE, SIZE, true);
    }
}
