package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.figures.Endpoint;
import com.xrosstools.idea.gef.figures.Figure;

import java.awt.*;

public class HomolateralRouter extends AbstractRouter {
    private boolean vertical;
    private Endpoint adjuster;
    private int distance = 100;

    public HomolateralRouter(boolean vertical) {
        this.vertical = vertical;
        adjuster = new Endpoint(this);
    }

    @Override
    public boolean contains(Endpoint endpoint) {
        return endpoint == adjuster;
    }

    public int getDistance() {
        if(adjuster != null)
            adjuster.setAdjustment(null);
        return distance;
    }

    public void setDistance(int distance) {
        if(adjuster != null)
            adjuster.setAdjustment(null);
        this.distance = distance;
    }

    public void activate(Connection conn) {
        conn.add(adjuster, new MidpointLocator(1).setFigure(adjuster));
    }

    public void deactivate(Connection conn) {
        conn.remove(adjuster);
    }

    @Override
    public void routeDifferentNode(Connection conn) {
        Point adj = adjuster.getAdjustment();

        PointList pl = conn.getPoints();
        Point start = pl.getFirst();
        Point end = pl.getLast();
        pl.removeAllPoints();

        Point p1, p2;
        pl.addPoint(start);

        if(vertical) {
            //Loop back
            int y = start.x > end.x ? Math.min(start.y, end.y) : Math.max(start.y, end.y);

            distance = adj == null ? distance : Math.abs(adj.y - start.y);

            y = start.x > end.x ? y - distance : y + distance;
            p1 = new Point(start.x, y);
            p2 = new Point(end.x, y);
        }else {
            int x = start.y > end.y  ? Math.max(start.x, end.x) : Math.min(start.x, end.x);

            distance = adj == null ? distance : Math.abs(adj.x - x);

            x = start.y > end.y ? x + distance : x - distance;
            p1 = new Point(x, start.y);
            p2 = new Point(x, end.y);
        }

        pl.addPoint(p1);
        pl.addPoint(p2);

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

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof HomolateralRouter && ((HomolateralRouter)obj).vertical == vertical;
    }
}
