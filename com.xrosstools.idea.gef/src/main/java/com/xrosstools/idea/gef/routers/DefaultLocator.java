package com.xrosstools.idea.gef.routers;

import com.xrosstools.idea.gef.figures.Figure;

import java.awt.*;

public class DefaultLocator extends AbstractLocator {
    public DefaultLocator(Figure figure) {
        super(figure);
    }

    @Override
    public Point getLocation(PointList points) {
        return middleOf(points.get(0), points.get(1));
    }
}
