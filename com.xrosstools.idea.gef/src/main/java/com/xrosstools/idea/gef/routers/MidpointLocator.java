package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Figure;
import java.awt.Point;

public class MidpointLocator implements ConnectionLocator {
    protected final int index;
    private Figure figure;

    public MidpointLocator() {
        this(0);
    }

    public MidpointLocator(int index) {
        this.index = index;
        this.figure = null;
    }

    @Override
    public Point getLocation(PointList points) {
        Point start = points.get(index);
        Point end = points.get(index + 1);

        Point loc = new Point((start.x + end.x) / 2, (start.y + end.y) / 2);
        if (figure != null) {
            loc.translate(-figure.getWidth() / 2, -figure.getHeight() / 2);
        }
        return loc;
    }

    public Figure getFigure() {
        return figure;
    }

    public MidpointLocator setFigure(Figure figure) {
        this.figure = figure;
        return this;
    }
}