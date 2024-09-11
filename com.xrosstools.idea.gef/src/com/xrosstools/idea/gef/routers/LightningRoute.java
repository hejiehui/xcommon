package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Connection;

import java.awt.*;

public class LightningRoute implements ConnectionRouter {
    private boolean vertical;

    public LightningRoute(boolean vertical) {
        this.vertical= vertical;
    }

    @Override
    public void route(Connection conn) {
        PointList pl = conn.getPoints();
        Point start = pl.getFirst();
        Point end = pl.getLast();
        pl.removeAllPoints();

        pl.addPoint(start);
        if (vertical) {
            int y = (start.y + end.y)/2;
            pl.addPoint(new Point(start.x, y));
            pl.addPoint(new Point(end.x, y));
        } else {
            int x = (start.x + end.x)/2;
            pl.addPoint(new Point(x, start.y));
            pl.addPoint(new Point(x, end.y));
        }

        pl.addPoint(end);
    }
}