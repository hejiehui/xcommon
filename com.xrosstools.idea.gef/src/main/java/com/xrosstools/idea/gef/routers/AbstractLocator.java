package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Figure;

import java.awt.*;

public abstract class AbstractLocator implements ConnectionLocator{
    public static int H_GAP = 10;
    public static int V_GAP = 5;
    private Figure figure;

    public AbstractLocator(Figure figure) {
        this.figure = figure;
    }

    public boolean isEnoughWidth(Point start, Point end) {
        return Math.abs(start.x - end.x) - 2 * H_GAP > figure.getWidth();
    }

    public Point middleOf(Point start, Point end) {
        int x = (start.x + end.x)/2;
        int y = (start.y + end.y)/2;

        if(start.x == end.x)
            return new Point(x + H_GAP, y - figure.getHeight()/2);

        if(start.y == end.y)
            return new Point(x - figure.getWidth()/2, y -figure.getHeight() - V_GAP);

        boolean leftOf = onLeft(start, end);
        boolean upOf = onTop(start, end);
        if(leftOf && upOf || !leftOf && !upOf)
            return new Point(x + H_GAP, y -figure.getHeight() - V_GAP);

        return new Point(x + H_GAP, y + V_GAP);
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

    public Point rightOf(Point pos) {
        return new Point(pos.x + H_GAP, pos.y - figure.getHeight() - V_GAP);
    }

    public Point leftOf(Point pos) {
        return new Point(pos.x - H_GAP - figure.getWidth(), pos.y - figure.getHeight() - V_GAP);
    }
}
