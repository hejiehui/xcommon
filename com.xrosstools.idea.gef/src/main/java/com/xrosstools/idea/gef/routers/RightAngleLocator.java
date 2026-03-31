package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Figure;

import java.awt.*;

public class RightAngleLocator extends AbstractLocator {
    private boolean vertical;
    public RightAngleLocator(Figure figure, boolean vertical) {
        super(figure);
        this.vertical = vertical;
    }

    @Override
    public Point getLocation(PointList points) {
        if(vertical) {
             if(points.size() == 2)
                 return middleOf(points.get(0), points.get(1));

            Point start = points.get(1);
            Point end = points.get(2);
             // Point to self
             if(points.size() == 4)
                 return rightOf(start);
             else {
                 if (isEnoughWidth(start, end))
                     return onLeft(start, end) ? rightOf(start) : leftOf(start);
                 else
                     return onLeft(start, end) ? leftOf(end) : rightOf(end);
             }
        }else {
            if(points.size() == 2)
                return middleOf(points.get(0), points.get(1));

            Point start = points.get(0);
            Point end = points.get(1);
            return onLeft(start, end) ? rightOf(start) : leftOf(start);
        }
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }
}
