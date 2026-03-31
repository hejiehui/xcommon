package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Figure;

import java.awt.*;

public class HomolateralLocator extends AbstractLocator {
    private boolean vertical;
    public HomolateralLocator(Figure figure, boolean vertical) {
        super(figure);
        this.vertical = vertical;
    }

    @Override
    public Point getLocation(PointList points) {
        if(vertical) {
            Point start;
            Point end;
            if(points.size() == 2) {
                start = points.get(0);
                end = points.get(1);
            } else {
                start = points.get(1);
                end = points.get(2);
                // Point to self
                if (points.size() == 5)
                    return rightOf(start);
            }
            return onLeft(start, end) ? rightOf(start) : leftOf(start);
        }else {
            Point start = points.get(0);
            Point end = points.get(1);
            if(points.size() == 2 && start.x == end.x && onTop(start, end))
                return new Point(start.x + V_GAP, start.y);
            else
                return onLeft(start, end) ? rightOf(start) : leftOf(start);
        }
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }
}
