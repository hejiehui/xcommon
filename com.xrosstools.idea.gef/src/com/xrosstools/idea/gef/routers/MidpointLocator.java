package com.xrosstools.idea.gef.routers;

import java.awt.*;

public class MidpointLocator implements ConnectionLocator {
    private int index;

    public MidpointLocator() {
        this(0);
    }

    public MidpointLocator(int i) {
        index = i;
    }

    protected int getIndex() {
        return index;
    }

    @Override
    public Point getLocation(PointList points) {
        Point start = points.get(index);
        Point end = points.get(index + 1);
        return new Point((start.x + end.x)/2, (start.y + end.y)/2);
    }
}
