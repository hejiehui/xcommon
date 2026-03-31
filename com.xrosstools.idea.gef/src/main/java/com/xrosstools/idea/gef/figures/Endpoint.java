package com.xrosstools.idea.gef.figures;

import com.xrosstools.idea.gef.parts.AbstractGraphicalEditPart;
import com.xrosstools.idea.gef.routers.ConnectionRouter;

import java.awt.*;

public class Endpoint extends Figure {
    public static final int SIZE = 8;
    private ConnectionRouter router;
    private Point adjustment;

    public Endpoint() {
        setWidth(SIZE);
        setHeight(SIZE);
    }

    public Endpoint(ConnectionRouter router) {
        this();
        this.router = router;
    }

    public void setAdjustment(Point adjustment) {
        this.adjustment = adjustment;
    }

    public Point getAdjustment() {
        return adjustment;
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

    public boolean isConnectionAdjusterEndpoint() {
        return isConnectionEndpoint() && getParentConnection().getRouter().contains(this);
    }

    @Override
    public boolean isSelectable() {
        //If connection has no edit policy, then end point should not be selectable
        if(getPart() != null && getPart().getEditPolicy() != null)
            return isConnectionSourceEndpoint() || isConnectionTargetEndpoint() || isConnectionAdjusterEndpoint();
        else
            return false;
    }

    @Override
    public void paint(Graphics graphics) {
        if(isSelectable() && getParentConnection().isSelected()) {
//            if(isConnectionAdjusterEndpoint())
//                graphics.fill3DRect(getX()-SIZE/2, getY()-SIZE/2, SIZE, SIZE, true);
//            else
                graphics.fill3DRect(getX(), getY(), SIZE, SIZE, true);
        }
    }
}
