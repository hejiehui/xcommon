package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Connection;
import com.xrosstools.idea.gef.figures.Figure;

import java.awt.*;

public class LightningRouter extends AbstractRouter {
    private boolean vertical;

    public LightningRouter(boolean vertical) {
        this.vertical = vertical;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof LightningRouter && ((LightningRouter)obj).vertical == vertical;
    }

    @Override
    public void routeDifferentNode(Connection conn) {
        PointList pl = conn.getPoints();
        Point start = pl.getFirst();
        Point end = pl.getLast();
        pl.removeAllPoints();

        int y = (start.y + end.y)/2;
        int x = (start.x + end.x)/2;
        Point p1 = vertical ? new Point(start.x, y) : new Point(x, start.y);
        Point p2 = vertical ? new Point(end.x, y) : new Point(x, end.y);

        Object source = conn.getSource();
        Object target = conn.getTarget();
        if((source instanceof Figure) && (target instanceof Figure)) {
            Figure sourceFigure = (Figure)source;
            Figure targetFigure = (Figure)target;
            Point _p1 = new Point(p1);
            sourceFigure.translateToRelative(_p1);
            if(sourceFigure.containsPoint(_p1) ||
                    _p1.x == sourceFigure.getLeft().x ||
                    _p1.x == sourceFigure.getRight().x ||
                    _p1.y == sourceFigure.getTop().y ||
                    _p1.y == sourceFigure.getBottom().y) {
                if(vertical) {
                    if(_p1.x == sourceFigure.getCenter().x) {
                        p1.x += onLeft(p1, p2) ? sourceFigure.getWidth() / 2 : -sourceFigure.getWidth() / 2;
                        p2.x += onLeft(p1, p2) ? -targetFigure.getWidth() / 2 : targetFigure.getWidth() / 2;
                    }
                }else{
                    if(_p1.y == sourceFigure.getCenter().y) {
                        p1.y += onTop(p1, p2) ? sourceFigure.getHeight() / 2 : -sourceFigure.getHeight() / 2;
                        p2.y += onTop(p1, p2) ? -targetFigure.getHeight() / 2 : targetFigure.getHeight() / 2;
                    }
                }
                pl.addPoint(p1);
                pl.addPoint(p2);
            }else {
                pl.addPoint(start);
                pl.addPoint(p1);
                pl.addPoint(p2);
                pl.addPoint(end);
            }
        }else {
            if(!start.equals(p1))
                pl.addPoint(start);
            pl.addPoint(p1);
            pl.addPoint(p2);
            if(!end.equals(p2))
                pl.addPoint(end);
        }
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
            end = figure.getBottom();
            Point right = figure.getRight();

            pl.addPoint(start);
            pl.addPoint(new Point(start.x, start.y + gap));
            pl.addPoint(new Point(right.x + gap, start.y + gap));
            pl.addPoint(new Point(right.x + gap, end.y - gap));
            pl.addPoint(new Point(end.x, end.y - gap));
            pl.addPoint(end);
        } else {
            start = figure.getRight();
            end = figure.getLeft();
            Point bottom = figure.getBottom();

            pl.addPoint(start);
            pl.addPoint(new Point(start.x + gap, start.y));
            pl.addPoint(new Point(start.x + gap, bottom.y + gap));
            pl.addPoint(new Point(end.x - gap, bottom.y + gap));
            pl.addPoint(new Point(end.x - gap, end.y));
            pl.addPoint(end);
        }

        Figure.translateToAbsolute(figure, pl);
    }

    public boolean onLeft(Point start, Point end) {
        return start.x <= end.x;
    }

    public boolean onRight(Point start, Point end) {
        return !onLeft(start, end);
    }

    public boolean onTop(Point start, Point end) {
        return start.y <= end.y;
    }

    public boolean onBottom(Point start, Point end) {
        return !onTop(start, end);
    }
}
