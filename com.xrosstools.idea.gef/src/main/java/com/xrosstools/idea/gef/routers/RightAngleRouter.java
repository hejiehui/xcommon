package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.figures.Figure;

import java.awt.*;

public class RightAngleRouter extends AbstractRouter {
    public static final int HEIGHT_FIRST = 0;
    public static final int WIDTH_FIRST = 1;

    private boolean vertical;

    @Deprecated
    public RightAngleRouter(int style) {
        if(style != 0 && style != 1)
            throw new IllegalArgumentException("Style is not a legal value");

        this.vertical = style == HEIGHT_FIRST;
    }

    public RightAngleRouter(boolean vertical) {
        this.vertical = vertical;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof RightAngleRouter && ((RightAngleRouter)obj).vertical == vertical;
    }

    @Override
    public void routeDifferentNode(Connection conn) {
        PointList pl = conn.getPoints();
        Point start = pl.getFirst();
        Point end = pl.getLast();
        pl.removeAllPoints();

        Point middle;
        pl.addPoint(start);

        middle = vertical ? new Point(start.x, end.y) : new Point(end.x, start.y);

        if(!(start.x == end.x || start.y == end.y))
            pl.addPoint(middle);

        pl.addPoint(end);
    }

    @Override
    public void routeSameNode(Connection conn) {
        Figure figure = conn.hasFeedback() ? (Figure) conn.getFeedback() : conn.getConnectionPart().getSourceFigure();
        PointList pl = conn.getPoints();
        Point start;
        Point end;
        pl.removeAllPoints();

        int gap = indexOfSameStyle(conn) * 50;

        if (vertical) {
            start = figure.getTop();
            end = figure.getRight();
            pl.addPoint(start);
            pl.addPoint(new Point(start.x, end.y - gap));
            pl.addPoint(new Point(end.x + gap, end.y - gap));
            pl.addPoint(new Point(end.x + gap, end.y));
            pl.addPoint(end);
        } else {
            start = figure.getRight();
            end = figure.getBottom();
            pl.addPoint(start);
            pl.addPoint(new Point(start.x + gap, start.y));
            pl.addPoint(new Point(start.x + gap, start.y + gap));
            pl.addPoint(new Point(end.x, start.y + gap));
            pl.addPoint(end);
        }

        Figure.translateToAbsolute(figure, pl);
    }
}