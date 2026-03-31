package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.figures.Figure;

import java.awt.*;

public class DefaultRouter extends AbstractRouter {
    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof DefaultRouter;
    }

    @Override
    public void routeDifferentNode(Connection conn) {
        PointList pl = conn.getPoints();
        Point start = pl.getFirst();
        Point end = pl.getLast();
        //For Idea Gef, you need to remove all points after get start and end
        pl.removeAllPoints();

        pl.addPoint(start);
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

        start = figure.getLeft();
        end = figure.getBottom();
        pl.addPoint(start);
        pl.addPoint(new Point(start.x - gap, start.y));
        pl.addPoint(new Point(start.x - gap, start.y + gap));
        pl.addPoint(new Point(end.x, start.y + gap));
        pl.addPoint(end);

        Figure.translateToAbsolute(figure, pl);
    }
}
